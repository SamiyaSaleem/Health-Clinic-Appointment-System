package com.example.demo.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.entity.Patient;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByEmail(String email);

    boolean existsByEmail(String email);
}