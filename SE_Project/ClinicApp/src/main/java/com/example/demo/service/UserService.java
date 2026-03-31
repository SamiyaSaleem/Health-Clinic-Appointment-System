package com.example.demo.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.demo.entity.User;
import com.example.demo.entity.Role;
import com.example.demo.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerPatient(String fullName, String email, String phone, String address, LocalDate dateOfBirth, String password) {
        if (repo.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setAddress(address);
        user.setDateOfBirth(dateOfBirth);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(Role.PATIENT);
        user.setEnabled(true);

        return repo.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return repo.findByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return repo.findById(id);
    }

    public User updateProfile(Long id, String phone, String address) {
        User user = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setPhone(phone);
        user.setAddress(address);
        return repo.save(user);
    }

    public void changePassword(Long id, String newPassword) {
        User user = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        repo.save(user);
    }

    public void unlockAccount(Long id) {
        User user = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);
        repo.save(user);
    }

    public List<User> getAllPatients() {
        return repo.findByRole(Role.PATIENT);
    }

    public List<User> getAllDoctors() {
        return repo.findByRole(Role.DOCTOR);
    }

    public User save(User user) {
        return repo.save(user);
    }
}
