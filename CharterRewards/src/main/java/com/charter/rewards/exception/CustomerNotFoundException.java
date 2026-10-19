package com.charter.rewards.exception;

/**
 * Thrown when someone asks for a customer that doesn't exist.
 *
 * The GlobalExceptionHandler catches this and turns it into a clean
 * 404 response so the caller knows exactly what went wrong.
 */
public class CustomerNotFoundException extends RuntimeException {

    /**
     * @param customerId the ID we looked for but couldn't find
     */
    public CustomerNotFoundException(Long customerId) {
        super("Customer not found with id: " + customerId);
    }
}
