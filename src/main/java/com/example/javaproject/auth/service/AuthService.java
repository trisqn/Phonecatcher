package com.example.javaproject.auth.service;

import com.example.javaproject.auth.dto.LoginRequest;
import com.example.javaproject.auth.dto.RegisterRequest;
import com.example.javaproject.auth.dto.TokenResponse;
import com.example.javaproject.auth.dto.UserResponse;
import com.example.javaproject.auth.security.AccountPrincipal;
import com.example.javaproject.entity.User;
import com.example.javaproject.entity.UserRole;
import com.example.javaproject.repository.UserRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

/**
 * Handles account registration, credential verification, and JWT issuance.
 */
@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private static final Duration ACCESS_TOKEN_LIFETIME = Duration.ofMinutes(15);
    private static final String EMAIL_UNIQUE_INDEX = "ux_app_user_email_lower";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final Clock clock;

    /**
     * Creates the service with account, credential, authentication, and token components.
     *
     * @param userRepository persistent account storage
     * @param passwordEncoder password hashing implementation
     * @param authenticationManager credential authentication provider
     * @param jwtEncoder access-token encoder
     * @param clock source of token timestamps
     */
    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtEncoder jwtEncoder,
            Clock clock
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.clock = clock;
    }

    /**
     * Creates a normalized USER account and stores only its BCrypt password hash.
     *
     * @param request validated registration details
     * @return the created account
     * @throws EmailAlreadyRegisteredException if the email is already in use
     * @throws InvalidPasswordException if the password exceeds BCrypt's byte limit
     */
    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (!isBcryptLengthValid(request.password())) {
            throw new InvalidPasswordException();
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyRegisteredException();
        }

        User user = new User(email, passwordEncoder.encode(request.password()), UserRole.USER);
        try {
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            if (isEmailUniqueViolation(exception)) {
                throw new EmailAlreadyRegisteredException();
            }
            throw exception;
        }
        logger.info("User account created: userId={}, role={}", user.getId(), user.getRole());
        return new UserResponse(user.getId(), user.getEmail(), user.getRole(), user.getCreatedAt());
    }

    /**
     * Authenticates credentials and creates a 15-minute signed JWT.
     *
     * @param request email and password credentials
     * @return a Bearer access token and expiration timestamp
     * @throws org.springframework.security.authentication.BadCredentialsException if credentials are invalid
     */
    public TokenResponse login(LoginRequest request) {
        if (!isBcryptLengthValid(request.password())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        String email = normalizeEmail(request.email());
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(email, request.password())
        );
        AccountPrincipal principal = (AccountPrincipal) authentication.getPrincipal();
        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plus(ACCESS_TOKEN_LIFETIME);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("JavaProject")
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(principal.getUsername())
                .claim("roles", List.of(principal.getRole().name()))
                .build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(),
                claims
        )).getTokenValue();
        logger.info("User authenticated: userId={}, role={}", principal.getId(), principal.getRole());
        return new TokenResponse(token, "Bearer", expiresAt);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isBcryptLengthValid(String password) {
        return password.getBytes(StandardCharsets.UTF_8).length <= 72;
    }

    private boolean isEmailUniqueViolation(DataIntegrityViolationException exception) {
        Throwable cause = exception;
        while (cause != null) {
            if (cause instanceof ConstraintViolationException constraintViolation
                    && EMAIL_UNIQUE_INDEX.equals(constraintViolation.getConstraintName())) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
