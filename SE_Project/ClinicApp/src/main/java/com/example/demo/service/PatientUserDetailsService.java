package com.example.demo.service;

import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.security.authentication.LockedException;

@Service
public class PatientUserDetailsService implements UserDetailsService {

    private final UserRepository repo;

    public PatientUserDetailsService(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User u = repo.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));

        if (u.isAccountLocked()) {
            throw new LockedException("Your account has been locked due to too many failed attempts. Please contact admin.");
        }

        return org.springframework.security.core.userdetails.User
            .withUsername(u.getEmail())
            .password(u.getPasswordHash())
            .roles(u.getRole().toString())
            .accountLocked(!u.isEnabled())
            .build();
    }
}