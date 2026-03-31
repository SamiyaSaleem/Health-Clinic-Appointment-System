package com.example.demo.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.example.demo.entity.Appointment;
import com.example.demo.entity.AppointmentStatus;
import com.example.demo.entity.Doctor;
import com.example.demo.entity.User;
import com.example.demo.repository.AppointmentRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {

    private final AppointmentRepository repo;

    public AppointmentService(AppointmentRepository repo) {
        this.repo = repo;
    }

    public Appointment bookAppointment(User patient, Doctor doctor, LocalDate appointmentDate, String time, String reason) {
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(appointmentDate);
        appointment.setReason(reason);
        appointment.setStatus(AppointmentStatus.PENDING);
        return repo.save(appointment);
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
        appt.setStatus(AppointmentStatus.CONFIRMED);
        repo.save(appt);
    }

    public void completeAppointment(Long id, String notes) {
        Appointment appt = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        appt.setStatus(AppointmentStatus.COMPLETED);
        appt.setNotes(notes);
        repo.save(appt);
    }

    public void markNoShow(Long id) {
        Appointment appt = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        appt.setStatus(AppointmentStatus.NO_SHOW);
        repo.save(appt);
    }

    public void cancelAppointment(Long id, String cancellationReason) {
        Appointment appt = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        appt.setStatus(AppointmentStatus.CANCELLED);
        appt.setCancellationReason(cancellationReason);
        repo.save(appt);
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
