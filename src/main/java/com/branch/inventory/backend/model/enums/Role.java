package com.branch.inventory.backend.model.enums;

/**
 * User roles for RBAC enforcement.
 * FR-03: Role-Based Access Control on every API endpoint.
 */
public enum Role {
    STAFF,
    MANAGER,
    HO_ADMIN,
    ACCOUNTANT,
    ADMIN
}