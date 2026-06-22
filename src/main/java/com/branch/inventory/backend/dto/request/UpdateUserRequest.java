package com.branch.inventory.backend.dto.request;

import com.branch.inventory.backend.model.enums.Role;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for PUT /api/v1/users/{id}
 *
 * branchId has three states:
 * - key absent in JSON → branchIdProvided=false → don't touch branch
 * - key present, value null → branchIdProvided=true, branchId=null → unassign
 * branch
 * - key present, value set → branchIdProvided=true, branchId=X → assign branch
 * X
 *
 * We can't use @Data here because the custom @JsonSetter on setBranchId()
 * must not be overwritten by Lombok's generated setter.
 */
@Getter
@Setter
public class UpdateUserRequest {

    private String fullName;

    @Email(message = "Invalid email format")
    private String email;

    private Role role;

    private Long branchId;
    private boolean branchIdProvided = false;

    /**
     * Custom setter so Jackson flips branchIdProvided=true
     * whenever the "branchId" key appears in the request body,
     * even when its value is null.
     */
    @JsonSetter(value = "branchId", nulls = Nulls.AS_EMPTY)
    public void setBranchId(Long branchId) {
        this.branchId = branchId;
        this.branchIdProvided = true;
    }
}
