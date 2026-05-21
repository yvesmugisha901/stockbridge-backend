package com.branch.inventory.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApprovalDecisionRequest {

    @NotNull(message = "Decision is required")
    private boolean approved; // boolean (primitive) so Lombok generates isApproved()

    private String comments; // renamed from "comment" to match service's getComments()
}