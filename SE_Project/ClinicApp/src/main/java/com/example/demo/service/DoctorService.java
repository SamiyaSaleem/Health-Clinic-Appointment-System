package com.example.demo.service;

import org.springframework.stereotype.Service;
import com.example.demo.entity.Doctor;
import com.example.demo.repository.DoctorRepository;
import java.util.List;
import java.util.Optional;

@Service
public class DoctorService {

    private final DoctorRepository repo;

    public DoctorService(DoctorRepository repo) {
        this.repo = repo;
    }

    public Doctor createDoctor(Doctor doctor) {
        doctor.setApproved(false);
        doctor.setActive(true);
        return repo.save(doctor);
    }

    public Optional<Doctor> findById(Long id) {
        return repo.findById(id);
    }

    public Optional<Doctor> findByUserId(Long userId) {
        return repo.findByUserId(userId);
    }

    public List<Doctor> getAllApprovedAndActive() {
        return repo.findByIsApprovedAndIsActive(true, true);
    }

    public List<Doctor> getBySpecialization(String specialization) {
        return repo.findBySpecializationAndIsApprovedAndIsActive(specialization, true, true);
    }

    public List<Doctor> getPendingApprovals() {
        return repo.findByIsApproved(false);
    }

    public List<Doctor> getAllDoctors() {
        return repo.findAll();
    }

    public void approveDoctor(Long id) {
        Doctor doctor = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Doctor not found"));
        doctor.setApproved(true);
        repo.save(doctor);
    }

    public void rejectDoctor(Long id) {
        repo.deleteById(id);
    }

    public void toggleActive(Long id) {
        Doctor doctor = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Doctor not found"));
        doctor.setActive(!doctor.isActive());
        repo.save(doctor);
    }

    public Doctor updateDoctor(Doctor doctor) {
        return repo.save(doctor);
    }

    public List<Doctor> getActiveDoctors() {
        return repo.findByIsActive(true);
    }
}
