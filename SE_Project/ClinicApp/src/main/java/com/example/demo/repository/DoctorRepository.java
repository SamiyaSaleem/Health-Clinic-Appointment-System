package com.example.demo.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.entity.Doctor;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByUserId(Long userId);

    List<Doctor> findByIsApprovedAndIsActive(boolean isApproved, boolean isActive);

    List<Doctor> findBySpecializationAndIsApprovedAndIsActive(String specialization, boolean isApproved, boolean isActive);

    List<Doctor> findByIsApproved(boolean isApproved);

    List<Doctor> findByIsActive(boolean isActive);
}
