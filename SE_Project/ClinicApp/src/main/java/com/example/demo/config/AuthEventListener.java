package com.example.demo.config;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import com.example.demo.service.LoginAttemptService;

@Component
public class AuthEventListener {

    private final LoginAttemptService attempts;

    public AuthEventListener(LoginAttemptService attempts) {
        this.attempts = attempts;
    }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent e) {
        String email = e.getAuthentication().getName();
        attempts.loginSucceeded(email);
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent e) {
        Object principal = e.getAuthentication().getPrincipal();
        if (principal instanceof String email) {
            attempts.loginFailed(email);
        }
    }
}