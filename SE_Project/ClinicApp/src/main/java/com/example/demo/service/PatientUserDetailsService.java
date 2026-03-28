package com.example.demo.service;

import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Patient;
import com.example.demo.repository.PatientRepository;
import org.springframework.security.authentication.LockedException;

@Service
public class PatientUserDetailsService implements UserDetailsService {

    private final PatientRepository repo;

    public PatientUserDetailsService(PatientRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Patient p = repo.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));

        if (p.isAccountLocked()) {
            throw new LockedException("Account locked");
        }

        return org.springframework.security.core.userdetails.User
            .withUsername(p.getEmail())
            .password(p.getPasswordHash())
            .roles("PATIENT")
            .build();
    }
}