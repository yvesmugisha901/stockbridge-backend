package com.branch.inventory.backend.exception;

// ── ResourceNotFoundException ─────────────────────────────────────────────────
// Thrown when a requested entity (user, branch, item, transfer) does not exist.
// Maps to HTTP 404 in GlobalExceptionHandler.

class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " not found with id: " + id);
    }

    public ResourceNotFoundException(String resource, String field, Object value) {
        super(resource + " not found with " + field + ": " + value);
    }
}