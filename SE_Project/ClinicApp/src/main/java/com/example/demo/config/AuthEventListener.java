package com.example.demo.config;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import com.example.demo.service.LoginAttemptService;

@Component
public class AuthEventListener {

    private final LoginAttemptService loginAttemptService;

    public AuthEventListener(LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
    }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent e) {
        String email = e.getAuthentication().getName();
        loginAttemptService.loginSucceeded(email);
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent e) {
        Object principal = e.getAuthentication().getPrincipal();
        if (principal instanceof String email) {
            loginAttemptService.loginFailed(email);
        }
    }
}