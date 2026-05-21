package com.branch.inventory.backend.exception;

/**
 * Thrown when a transfer request is submitted for a quantity that exceeds
 * the available stock at the source branch (UC-06, Alternative Flow 3a).
 * Maps to HTTP 409 Conflict in GlobalExceptionHandler.
 */
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String message) {
        super(message);
    }

    public InsufficientStockException(String itemName, String branchName,
            int requested, int available) {
        super(String.format(
                "Insufficient stock for item '%s' at branch '%s'. Requested: %d, Available: %d.",
                itemName, branchName, requested, available));
    }
}