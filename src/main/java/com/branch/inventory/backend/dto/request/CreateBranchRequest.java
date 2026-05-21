package com.branch.inventory.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateBranchRequest {

    @NotBlank(message = "Branch name is required")
    private String name;

    @NotBlank(message = "Branch code is required")
    private String code;

    private String location;

    private String contactInfo; // renamed from "contactPhone" to match service's getContactInfo()
}