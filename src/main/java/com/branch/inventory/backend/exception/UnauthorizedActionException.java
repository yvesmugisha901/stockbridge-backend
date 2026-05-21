package com.branch.inventory.backend.exception;

/**
 * Thrown when an authenticated user attempts an action outside their role's
 * permissions (e.g. a STAFF user trying to approve a transfer).
 * Maps to HTTP 403 Forbidden in GlobalExceptionHandler — satisfies FR-03.
 */
public class UnauthorizedActionException extends RuntimeException {

    public UnauthorizedActionException(String message) {
        super(message);
    }

    public UnauthorizedActionException(String role, String action) {
        super(String.format("Role '%s' is not permitted to perform action: %s", role, action));
    }
}