package com.example.demo.service;

import org.springframework.stereotype.Service;
import com.example.demo.repository.UserRepository;

@Service
public class LoginAttemptService {

    private final UserRepository repo;

    public LoginAttemptService(UserRepository repo) {
        this.repo = repo;
    }

    public void loginSucceeded(String email) {
        repo.findByEmail(email).ifPresent(u -> {
            u.setFailedLoginAttempts(0);
            u.setAccountLocked(false);
            repo.save(u);
        });
    }

    public void loginFailed(String email) {
        repo.findByEmail(email).ifPresent(u -> {
            int attempts = u.getFailedLoginAttempts() + 1;
            u.setFailedLoginAttempts(attempts);

            if (attempts >= 5) {
                u.setAccountLocked(true);
            }
            repo.save(u);
        });
    }
}