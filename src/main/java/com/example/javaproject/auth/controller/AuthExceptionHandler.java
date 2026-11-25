package com.example.javaproject.auth.controller;

import com.example.javaproject.auth.service.EmailAlreadyRegisteredException;
import com.example.javaproject.auth.service.InvalidPasswordException;
import com.example.javaproject.category.service.CategoryCodeAlreadyExistsException;
import com.example.javaproject.category.service.CategoryNotFoundException;
import com.example.javaproject.dispute.service.DisputeAlreadyReviewedException;
import com.example.javaproject.dispute.service.DisputeNotFoundException;
import com.example.javaproject.dispute.service.DuplicateOpenDisputeException;
import com.example.javaproject.dispute.service.InvalidDisputeDecisionException;
import com.example.javaproject.dispute.service.WarningNotActiveException;
import com.example.javaproject.dispute.service.WarningNotFoundException;
import com.example.javaproject.phonenumber.service.InvalidPhoneNumberException;
import com.example.javaproject.report.service.CategoryNotAvailableException;
import com.example.javaproject.report.service.IncidentDateInFutureException;
import com.example.javaproject.report.service.ReporterNotFoundException;
import com.example.javaproject.warning.service.InvalidSyncTimeException;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Converts authentication and application exceptions into consistent API errors.
 */
@RestControllerAdvice
public class AuthExceptionHandler {

    /**
     * Creates the centralized API exception handler.
     */
    public AuthExceptionHandler() {
    }

    /**
     * Maps duplicate email registration to an HTTP conflict response.
     *
     * @param exception duplicate-email failure
     * @return the API error response
     */
    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    ResponseEntity<ApiError> handleDuplicateEmail(EmailAlreadyRegisteredException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError(409, "EMAIL_ALREADY_REGISTERED", exception.getMessage(), Map.of()));
    }

    /**
     * Maps an invalid password to a client-error response.
     *
     * @param exception password validation failure
     * @return the API error response
     */
    @ExceptionHandler(InvalidPasswordException.class)
    ResponseEntity<ApiError> handleInvalidPassword(InvalidPasswordException exception) {
        return ResponseEntity.badRequest()
                .body(new ApiError(400, "INVALID_PASSWORD", exception.getMessage(), Map.of()));
    }

    /**
     * Maps malformed report values to a client-error response.
     *
     * @param exception invalid report input
     * @return the API error response
     */
    @ExceptionHandler({InvalidPhoneNumberException.class, IncidentDateInFutureException.class})
    ResponseEntity<ApiError> handleInvalidReportInput(RuntimeException exception) {
        return ResponseEntity.badRequest()
                .body(new ApiError(400, "INVALID_REPORT", exception.getMessage(), Map.of()));
    }

    /**
     * Maps an unavailable category to a not-found response.
     *
     * @param exception category lookup failure
     * @return the API error response
     */
    @ExceptionHandler(CategoryNotAvailableException.class)
    ResponseEntity<ApiError> handleUnavailableCategory(CategoryNotAvailableException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError(404, "CATEGORY_NOT_FOUND", exception.getMessage(), Map.of()));
    }

    /**
     * Maps a missing category to a not-found response.
     *
     * @param exception category lookup failure
     * @return the API error response
     */
    @ExceptionHandler(CategoryNotFoundException.class)
    ResponseEntity<ApiError> handleMissingCategory(CategoryNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError(404, "CATEGORY_NOT_FOUND", exception.getMessage(), Map.of()));
    }

    /**
     * Maps a duplicate category code to a conflict response.
     *
     * @param exception duplicate-code failure
     * @return the API error response
     */
    @ExceptionHandler(CategoryCodeAlreadyExistsException.class)
    ResponseEntity<ApiError> handleDuplicateCategoryCode(CategoryCodeAlreadyExistsException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError(409, "CATEGORY_CODE_ALREADY_EXISTS", exception.getMessage(), Map.of()));
    }

    /**
     * Maps a missing authenticated account to an unauthorized response.
     *
     * @param exception account lookup failure
     * @return the API error response
     */
    @ExceptionHandler(ReporterNotFoundException.class)
    ResponseEntity<ApiError> handleMissingReporter(ReporterNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiError(401, "ACCOUNT_NOT_FOUND", exception.getMessage(), Map.of()));
    }

    /**
     * Maps an invalid warning synchronization timestamp to a client-error response.
     *
     * @param exception invalid timestamp failure
     * @return the API error response
     */
    @ExceptionHandler(InvalidSyncTimeException.class)
    ResponseEntity<ApiError> handleInvalidSyncTime(InvalidSyncTimeException exception) {
        return ResponseEntity.badRequest()
                .body(new ApiError(400, "INVALID_SYNC_TIME", exception.getMessage(), Map.of()));
    }

    /**
     * Maps dispute state conflicts to an HTTP conflict response.
     *
     * @param exception dispute state conflict
     * @return the API error response
     */
    @ExceptionHandler({WarningNotActiveException.class, DisputeAlreadyReviewedException.class,
            DuplicateOpenDisputeException.class})
    ResponseEntity<ApiError> handleDisputeConflict(RuntimeException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError(409, "DISPUTE_CONFLICT", exception.getMessage(), Map.of()));
    }

    /**
     * Maps missing dispute resources to a not-found response.
     *
     * @param exception resource lookup failure
     * @return the API error response
     */
    @ExceptionHandler({WarningNotFoundException.class, DisputeNotFoundException.class})
    ResponseEntity<ApiError> handleDisputeResourceNotFound(RuntimeException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError(404, "RESOURCE_NOT_FOUND", exception.getMessage(), Map.of()));
    }

    /**
     * Maps an unsupported dispute decision to a client-error response.
     *
     * @param exception invalid decision failure
     * @return the API error response
     */
    @ExceptionHandler(InvalidDisputeDecisionException.class)
    ResponseEntity<ApiError> handleInvalidDisputeDecision(InvalidDisputeDecisionException exception) {
        return ResponseEntity.badRequest()
                .body(new ApiError(400, "INVALID_DISPUTE_DECISION", exception.getMessage(), Map.of()));
    }

    /**
     * Maps authentication failures without disclosing which credential was invalid.
     *
     * @return the API error response
     */
    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ApiError> handleAuthenticationFailure() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiError(401, "INVALID_CREDENTIALS", "Invalid email or password", Map.of()));
    }

    /**
     * Maps bean-validation failures and includes the first message for each field.
     *
     * @param exception request validation failure
     * @return the API error response with field-level details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleValidationFailure(MethodArgumentNotValidException exception) {
        Map<String, String> fieldErrors = exception.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        error -> error.getField(),
                        error -> error.getDefaultMessage() == null ? "Invalid value" : error.getDefaultMessage(),
                        (first, ignored) -> first
                ));
        return ResponseEntity.badRequest()
                .body(new ApiError(400, "VALIDATION_FAILED", "Request validation failed", fieldErrors));
    }

    /**
     * Error response shared by the application's REST endpoints.
     *
     * @param status HTTP status code
     * @param code stable machine-readable error identifier
     * @param message human-readable summary
     * @param fieldErrors validation messages keyed by request field
     */
    @Schema(description = "Standard API error response.")
    public record ApiError(
            @Schema(description = "HTTP status code.", example = "400") int status,
            @Schema(description = "Stable machine-readable error identifier.", example = "VALIDATION_FAILED") String code,
            @Schema(description = "Human-readable summary.") String message,
            @Schema(description = "Validation messages keyed by request field; empty for non-validation errors.")
            Map<String, String> fieldErrors
    ) {
    }
}
