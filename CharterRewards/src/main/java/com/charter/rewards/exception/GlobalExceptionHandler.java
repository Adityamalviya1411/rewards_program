package com.charter.rewards.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One place to handle all the errors so we don't scatter try-catch blocks
 * all over the controllers.
 *
 * Every error response comes back as JSON with a timestamp, status code,
 * short reason, and a message that actually tells you what went wrong.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Fires when someone requests a customer ID that doesn't exist in the database.
     *
     * @param ex carries the message with the missing ID
     * @return 404 with a JSON error body
     */
    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCustomerNotFound(CustomerNotFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Fires when the business logic rejects something — like a start date
     * that comes after the end date, or a negative transaction amount.
     *
     * @param ex carries the validation message
     * @return 400 with a JSON error body
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Fires when a required query param like startDate or endDate is missing entirely.
     *
     * @param ex tells us which parameter was left out
     * @return 400 with a JSON error body
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParam(MissingServletRequestParameterException ex) {
        return buildError(HttpStatus.BAD_REQUEST, "Missing required parameter: " + ex.getParameterName());
    }

    /**
     * Fires when a parameter value can't be converted to the expected type —
     * for example, passing "january" where a yyyy-MM-dd date is expected.
     *
     * @param ex tells us which parameter had the bad value
     * @return 400 with a JSON error body
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return buildError(HttpStatus.BAD_REQUEST,
                "Invalid value for parameter '" + ex.getName() + "': " + ex.getValue());
    }

    /**
     * Builds the standard error response body we use across all handlers.
     *
     * @param status the HTTP status to send back
     * @param message a plain-English explanation of what went wrong
     * @return the response entity ready to return
     */
    private ResponseEntity<Map<String, Object>> buildError(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
