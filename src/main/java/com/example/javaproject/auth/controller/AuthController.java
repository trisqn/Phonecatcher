package com.example.javaproject.auth.controller;

import com.example.javaproject.auth.dto.LoginRequest;
import com.example.javaproject.auth.dto.RegisterRequest;
import com.example.javaproject.auth.dto.TokenResponse;
import com.example.javaproject.auth.dto.UserResponse;
import com.example.javaproject.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * Exposes account registration, authentication, and current-user endpoints.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Create accounts, obtain an access token, and inspect the current account.")
public class AuthController {

    private final AuthService authService;

    /**
     * Creates the controller with its authentication service.
     *
     * @param authService account registration and authentication operations
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers a new account with the USER role.
     *
     * @param request validated account email and password
     * @return the newly created account without its password hash
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Register a user account",
            description = "Creates a USER account. New accounts cannot assign themselves moderator or administrator roles.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Account created"),
                    @ApiResponse(responseCode = "400", description = "Email or password validation failed",
                            content = @Content(schema = @Schema(implementation = AuthExceptionHandler.ApiError.class))),
                    @ApiResponse(responseCode = "409", description = "Email is already registered",
                            content = @Content(schema = @Schema(implementation = AuthExceptionHandler.ApiError.class)))
            })
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    /**
     * Authenticates an account and issues a short-lived JWT.
     *
     * @param request account credentials
     * @return the access token and its expiration time
     */
    @PostMapping("/login")

    @Operation(
            summary = "Sign in",
            description = "Authenticates with email and password. Use the returned accessToken as a Bearer token in protected operations.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Authentication successful; returns a short-lived access token"),
                    @ApiResponse(responseCode = "400", description = "Request validation failed",
                            content = @Content(schema = @Schema(implementation = AuthExceptionHandler.ApiError.class))),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials",
                            content = @Content(schema = @Schema(implementation = AuthExceptionHandler.ApiError.class)))
            })
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /**
     * Returns the identity and roles in the current access token.
     *
     * @param authentication authenticated principal supplied by Spring Security
     * @return current account email and granted roles
     */
    @GetMapping("/me")

    @Operation(
            summary = "Get the current account",
            description = "Returns the email and roles represented by the authenticated access token.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Current account"),
                    @ApiResponse(responseCode = "401", description = "Missing or invalid access token")
            })
    public CurrentUserResponse currentUser(
            @Parameter(hidden = true) Authentication authentication
    ) {
        List<String> roles = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .filter(authority -> authority.startsWith("ROLE_"))
                .toList();
        return new CurrentUserResponse(authentication.getName(), roles);
    }

    /**
     * Identity and roles represented by the authenticated access token.
     *
     * @param email current account email address
     * @param roles granted role authorities
     */
    public record CurrentUserResponse(String email, List<String> roles) {
    }
}
