package com.example.demo.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.example.demo.entity.Appointment;
import com.example.demo.entity.AppointmentStatus;
import com.example.demo.entity.CancelledBy;
import com.example.demo.entity.Doctor;
import com.example.demo.entity.User;
import com.example.demo.repository.AppointmentRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private final AppointmentRepository repo;

    public AppointmentService(AppointmentRepository repo) {
        this.repo = repo;
    }

    private static final Set<AppointmentStatus> ACTIVE_STATUSES = EnumSet.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public Appointment bookAppointment(User patient, Doctor doctor, LocalDate appointmentDate, LocalTime appointmentTime, String reason) {
        if (patient == null) {
            throw new IllegalArgumentException("Patient is required");
        }
        if (doctor == null) {
            throw new IllegalArgumentException("Doctor is required");
        }
        if (appointmentDate == null || appointmentTime == null) {
            throw new IllegalArgumentException("Appointment date and time are required");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Reason is required");
        }

        if (!doctor.isActive()) {
            throw new IllegalStateException("Doctor is not active");
        }
        if (!doctor.isApproved()) {
            throw new IllegalStateException("Doctor is not approved");
        }

        LocalDateTime requested = LocalDateTime.of(appointmentDate, appointmentTime);
        if (requested.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Appointment cannot be in the past");
        }

        // Availability + clash check: requested time must be one of the generated available slots.
        List<LocalTime> availableSlots = getAvailableSlots(doctor, appointmentDate);
        if (!availableSlots.contains(appointmentTime)) {
            throw new IllegalArgumentException("Selected time slot is not available");
        }

        boolean clash = repo.existsByDoctorAndAppointmentDateAndAppointmentTimeAndStatusIn(
                doctor,
                appointmentDate,
                appointmentTime,
                new ArrayList<>(ACTIVE_STATUSES)
        );
        if (clash) {
            throw new IllegalStateException("This time slot is already booked");
        }

        Appointment appointment = new Appointment(patient, doctor, appointmentDate, appointmentTime, reason);
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setUpdatedAt(LocalDateTime.now());
        return repo.save(appointment);
    }

    public List<String> getAvailableSlotStrings(Doctor doctor, LocalDate appointmentDate) {
        return getAvailableSlots(doctor, appointmentDate).stream()
                .map(t -> t.format(TIME_FORMAT))
                .collect(Collectors.toList());
    }

    public List<LocalTime> getAvailableSlots(Doctor doctor, LocalDate appointmentDate) {
        if (doctor == null || appointmentDate == null) {
            return List.of();
        }
        if (!doctor.isActive() || !doctor.isApproved()) {
            return List.of();
        }

        String availableDays = doctor.getAvailableDays();
        LocalTime start = doctor.getAvailableTimeStart();
        LocalTime end = doctor.getAvailableTimeEnd();
        Integer durationMinutes = doctor.getAppointmentDurationMinutes();

        if (availableDays == null || availableDays.trim().isEmpty() || start == null || end == null || durationMinutes == null || durationMinutes <= 0) {
            return List.of();
        }

        String dayToken = appointmentDate.getDayOfWeek().name().substring(0, 3);
        // stored like "MON,TUE,..."
        boolean dayAllowed = false;
        for (String token : availableDays.split(",")) {
            if (token == null) continue;
            if (token.trim().equalsIgnoreCase(dayToken)) {
                dayAllowed = true;
                break;
            }
        }
        if (!dayAllowed) {
            return List.of();
        }

        // Build all possible slot start times; last start must be <= end - duration
        LocalTime lastStart = end.minusMinutes(durationMinutes);
        if (lastStart.isBefore(start)) {
            return List.of();
        }

        List<LocalTime> allSlots = new ArrayList<>();
        for (LocalTime t = start; !t.isAfter(lastStart); t = t.plusMinutes(durationMinutes)) {
            allSlots.add(t);
        }

        // Remove already-booked active slots
        List<Appointment> booked = repo.findByDoctorAndAppointmentDateAndStatusIn(
                doctor,
                appointmentDate,
                new ArrayList<>(ACTIVE_STATUSES)
        );
        if (booked.isEmpty()) {
            return allSlots;
        }

        Set<LocalTime> bookedTimes = booked.stream()
                .map(Appointment::getAppointmentTime)
                .filter(t -> t != null)
                .collect(Collectors.toSet());

        return allSlots.stream().filter(t -> !bookedTimes.contains(t)).collect(Collectors.toList());
    }

    public Optional<Appointment> findById(Long id) {
        return repo.findById(id);
    }

    public List<Appointment> getPatientAppointments(User patient) {
        return repo.findByPatient(patient);
    }

    public List<Appointment> getDoctorAppointments(Doctor doctor) {
        return repo.findByDoctor(doctor);
    }

    public List<Appointment> getByStatus(AppointmentStatus status) {
        return repo.findByStatus(status);
    }

    public List<Appointment> getAllAppointments() {
        return repo.findAll();
    }

    public void confirmAppointment(Long id) {
        Appointment appt = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        validateTransition(appt.getStatus(), AppointmentStatus.CONFIRMED);
        appt.setStatus(AppointmentStatus.CONFIRMED);
        appt.setUpdatedAt(LocalDateTime.now());
        repo.save(appt);
    }

    public void completeAppointment(Long id, String notes) {
        Appointment appt = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        validateTransition(appt.getStatus(), AppointmentStatus.COMPLETED);
        appt.setStatus(AppointmentStatus.COMPLETED);
        appt.setNotes(notes);
        appt.setUpdatedAt(LocalDateTime.now());
        repo.save(appt);
    }

    public void markNoShow(Long id) {
        Appointment appt = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        validateTransition(appt.getStatus(), AppointmentStatus.NO_SHOW);
        appt.setStatus(AppointmentStatus.NO_SHOW);
        appt.setUpdatedAt(LocalDateTime.now());
        repo.save(appt);
    }

    public void cancelAppointment(Long id, String cancellationReason) {
        Appointment appt = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        validateTransition(appt.getStatus(), AppointmentStatus.CANCELLED);
        appt.setStatus(AppointmentStatus.CANCELLED);
        appt.setCancellationReason(cancellationReason);
        appt.setUpdatedAt(LocalDateTime.now());
        repo.save(appt);
    }

    public void approveAppointmentByDoctor(Doctor doctor, Long appointmentId) {
        if (doctor == null) {
            throw new IllegalArgumentException("Doctor is required");
        }
        if (!doctor.isActive()) {
            throw new IllegalStateException("Doctor is not active");
        }
        if (!doctor.isApproved()) {
            throw new IllegalStateException("Doctor is not approved");
        }

        Appointment appt = repo.findById(appointmentId).orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        if (appt.getDoctor() == null || appt.getDoctor().getId() == null || !appt.getDoctor().getId().equals(doctor.getId())) {
            throw new IllegalStateException("Not allowed");
        }
        validateTransition(appt.getStatus(), AppointmentStatus.CONFIRMED);
        appt.setStatus(AppointmentStatus.CONFIRMED);
        appt.setUpdatedAt(LocalDateTime.now());
        repo.save(appt);
    }

    public void rejectAppointmentByDoctor(Doctor doctor, Long appointmentId, String rejectionReason) {
        if (doctor == null) {
            throw new IllegalArgumentException("Doctor is required");
        }
        if (!doctor.isActive()) {
            throw new IllegalStateException("Doctor is not active");
        }
        if (!doctor.isApproved()) {
            throw new IllegalStateException("Doctor is not approved");
        }
        if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
            throw new IllegalArgumentException("Rejection reason is required");
        }

        Appointment appt = repo.findById(appointmentId).orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        if (appt.getDoctor() == null || appt.getDoctor().getId() == null || !appt.getDoctor().getId().equals(doctor.getId())) {
            throw new IllegalStateException("Not allowed");
        }
        if (appt.getStatus() != AppointmentStatus.PENDING) {
            throw new IllegalStateException("Only pending appointments can be rejected");
        }

        validateTransition(appt.getStatus(), AppointmentStatus.CANCELLED);
        appt.setStatus(AppointmentStatus.CANCELLED);
        appt.setCancelledBy(CancelledBy.DOCTOR);
        appt.setCancellationReason(rejectionReason.trim());
        appt.setUpdatedAt(LocalDateTime.now());
        repo.save(appt);
    }

    public void completeAppointmentByDoctor(Doctor doctor, Long appointmentId, String notes) {
        if (doctor == null) {
            throw new IllegalArgumentException("Doctor is required");
        }
        if (!doctor.isActive()) {
            throw new IllegalStateException("Doctor is not active");
        }
        if (!doctor.isApproved()) {
            throw new IllegalStateException("Doctor is not approved");
        }

        Appointment appt = repo.findById(appointmentId).orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        if (appt.getDoctor() == null || appt.getDoctor().getId() == null || !appt.getDoctor().getId().equals(doctor.getId())) {
            throw new IllegalStateException("Not allowed");
        }

        validateTransition(appt.getStatus(), AppointmentStatus.COMPLETED);
        appt.setStatus(AppointmentStatus.COMPLETED);
        appt.setNotes(notes);
        appt.setUpdatedAt(LocalDateTime.now());
        repo.save(appt);
    }

    public void markNoShowByDoctor(Doctor doctor, Long appointmentId) {
        if (doctor == null) {
            throw new IllegalArgumentException("Doctor is required");
        }
        if (!doctor.isActive()) {
            throw new IllegalStateException("Doctor is not active");
        }
        if (!doctor.isApproved()) {
            throw new IllegalStateException("Doctor is not approved");
        }

        Appointment appt = repo.findById(appointmentId).orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        if (appt.getDoctor() == null || appt.getDoctor().getId() == null || !appt.getDoctor().getId().equals(doctor.getId())) {
            throw new IllegalStateException("Not allowed");
        }

        validateTransition(appt.getStatus(), AppointmentStatus.NO_SHOW);
        appt.setStatus(AppointmentStatus.NO_SHOW);
        appt.setUpdatedAt(LocalDateTime.now());
        repo.save(appt);
    }

    public void cancelAppointmentByDoctor(Doctor doctor, Long appointmentId, String reason) {
        if (doctor == null) {
            throw new IllegalArgumentException("Doctor is required");
        }
        if (!doctor.isActive()) {
            throw new IllegalStateException("Doctor is not active");
        }
        if (!doctor.isApproved()) {
            throw new IllegalStateException("Doctor is not approved");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Cancellation reason is required");
        }

        Appointment appt = repo.findById(appointmentId).orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        if (appt.getDoctor() == null || appt.getDoctor().getId() == null || !appt.getDoctor().getId().equals(doctor.getId())) {
            throw new IllegalStateException("Not allowed");
        }

        validateTransition(appt.getStatus(), AppointmentStatus.CANCELLED);
        appt.setStatus(AppointmentStatus.CANCELLED);
        appt.setCancelledBy(CancelledBy.DOCTOR);
        appt.setCancellationReason(reason.trim());
        appt.setUpdatedAt(LocalDateTime.now());
        repo.save(appt);
    }

    private void validateTransition(AppointmentStatus from, AppointmentStatus to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Invalid appointment status");
        }
        if (from == AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Completed appointments cannot be changed");
        }
        if (from == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Cancelled appointments cannot be changed");
        }

        switch (to) {
            case CONFIRMED -> {
                if (from != AppointmentStatus.PENDING) {
                    throw new IllegalStateException("Only pending appointments can be confirmed");
                }
            }
            case COMPLETED, NO_SHOW -> {
                if (from != AppointmentStatus.CONFIRMED) {
                    throw new IllegalStateException("Only confirmed appointments can be completed/marked no-show");
                }
            }
            case CANCELLED -> {
                if (from != AppointmentStatus.PENDING && from != AppointmentStatus.CONFIRMED) {
                    throw new IllegalStateException("Appointment cannot be cancelled in its current state");
                }
            }
            case PENDING -> {
                // not used as a transition target in this app
                throw new IllegalStateException("Cannot transition back to pending");
            }
        }
    }

    public Page<Appointment> getAllAppointmentsPaginated(Pageable pageable) {
        return repo.findAll(pageable);
    }

    public Page<Appointment> getPatientAppointmentsPaginated(User patient, Pageable pageable) {
        return repo.findByPatient(patient, pageable);
    }

    public Page<Appointment> getDoctorAppointmentsPaginated(Doctor doctor, Pageable pageable) {
        return repo.findByDoctor(doctor, pageable);
    }

    public Page<Appointment> getAppointmentsByStatus(AppointmentStatus status, Pageable pageable) {
        return repo.findByStatus(status, pageable);
    }

    public Appointment save(Appointment appointment) {
        return repo.save(appointment);
    }
}
