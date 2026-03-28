package com.example.demo.service;

import org.springframework.stereotype.Service;
import com.example.demo.repository.PatientRepository;

@Service
public class LoginAttemptService {

    private final PatientRepository repo;

    public LoginAttemptService(PatientRepository repo) {
        this.repo = repo;
    }

    public void loginSucceeded(String email) {
        repo.findByEmail(email).ifPresent(p -> {
            p.setFailedAttempts(0);
            p.setAccountLocked(false);
            repo.save(p);
        });
    }

    public void loginFailed(String email) {
        repo.findByEmail(email).ifPresent(p -> {
            int attempts = p.getFailedAttempts() + 1;
            p.setFailedAttempts(attempts);

            if (attempts >= 5) {
                p.setAccountLocked(true);
            }
            repo.save(p);
        });
    }
}