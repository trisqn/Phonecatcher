package com.example.javaproject.auth.service;

import com.example.javaproject.auth.dto.RegisterRequest;
import com.example.javaproject.auth.dto.LoginRequest;
import com.example.javaproject.entity.User;
import com.example.javaproject.entity.UserRole;
import com.example.javaproject.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private UserRepository userRepository;
    private BCryptPasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        authenticationManager = mock(AuthenticationManager.class);
        authService = new AuthService(
                userRepository,
                passwordEncoder,
                authenticationManager,
                mock(JwtEncoder.class),
                Clock.systemUTC()
        );
    }

    @Test
    void registerNormalizesEmailHashesPasswordAndAssignsUserRole() {
        when(userRepository.existsByEmailIgnoreCase("person@example.test")).thenReturn(false);
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.register(new RegisterRequest(" Person@Example.Test ", "StrongPassword123!"));

        var savedUser = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(savedUser.capture());
        assertThat(savedUser.getValue().getEmail()).isEqualTo("person@example.test");
        assertThat(savedUser.getValue().getRole()).isEqualTo(UserRole.USER);
        assertThat(savedUser.getValue().getPasswordHash()).isNotEqualTo("StrongPassword123!");
        assertThat(passwordEncoder.matches(
                "StrongPassword123!",
                savedUser.getValue().getPasswordHash()
        )).isTrue();
    }

    @Test
    void registerRejectsAnExistingEmailWithoutSaving() {
        when(userRepository.existsByEmailIgnoreCase("person@example.test")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(
                new RegisterRequest("Person@example.test", "StrongPassword123!")
        )).isInstanceOf(EmailAlreadyRegisteredException.class);

        verify(userRepository, never()).saveAndFlush(any(User.class));
    }

    @Test
    void registerRejectsPasswordsLongerThanBcryptByteLimit() {
        assertThatThrownBy(() -> authService.register(
                new RegisterRequest("person@example.test", "é".repeat(37))
        )).isInstanceOf(InvalidPasswordException.class);

        verify(userRepository, never()).saveAndFlush(any(User.class));
        verify(userRepository, never()).existsByEmailIgnoreCase(eq("person@example.test"));
    }

    @Test
    void loginRejectsPasswordsLongerThanBcryptByteLimitAsInvalidCredentials() {
        assertThatThrownBy(() -> authService.login(
                new LoginRequest("person@example.test", "é".repeat(37))
        )).isInstanceOf(BadCredentialsException.class);

        verify(authenticationManager, never()).authenticate(any());
    }
}
