package com.example.demo.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.entity.Appointment;
import com.example.demo.entity.AppointmentStatus;
import com.example.demo.entity.Doctor;
import com.example.demo.entity.User;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Patient queries
    List<Appointment> findByPatient(User patient);
    List<Appointment> findByPatientOrderByAppointmentDateDesc(User patient, Pageable pageable);
    Long countByPatient(User patient);
    Long countByPatientAndStatus(User patient, AppointmentStatus status);
    Long countByPatientAndAppointmentDateGreaterThanEqualAndStatus(User patient, LocalDate date, AppointmentStatus status);

    // Doctor queries
    List<Appointment> findByDoctor(Doctor doctor);
    List<Appointment> findByDoctorOrderByAppointmentDateDesc(Doctor doctor, Pageable pageable);
    Long countByDoctor(Doctor doctor);
    Long countByDoctorAndStatus(Doctor doctor, AppointmentStatus status);
    Long countByDoctorAndAppointmentDateAndStatus(Doctor doctor, LocalDate date, AppointmentStatus status);
    Long countByDoctorAndAppointmentDateGreaterThanEqual(Doctor doctor, LocalDate date);

    // General queries
    List<Appointment> findByStatus(AppointmentStatus status);
    long countByStatus(AppointmentStatus status);
    Page<Appointment> findByStatus(AppointmentStatus status, Pageable pageable);
    Page<Appointment> findByPatient(User patient, Pageable pageable);
    Page<Appointment> findByDoctor(Doctor doctor, Pageable pageable);
    List<Appointment> findByAppointmentDateAndDoctor(LocalDate date, Doctor doctor);

    // Clash/slots helpers
    boolean existsByDoctorAndAppointmentDateAndAppointmentTimeAndStatusIn(Doctor doctor, LocalDate appointmentDate, LocalTime appointmentTime, List<AppointmentStatus> statuses);
    List<Appointment> findByDoctorAndAppointmentDateAndStatusIn(Doctor doctor, LocalDate appointmentDate, List<AppointmentStatus> statuses);
}