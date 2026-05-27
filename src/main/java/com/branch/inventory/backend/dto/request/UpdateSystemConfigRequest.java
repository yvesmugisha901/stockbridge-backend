package com.branch.inventory.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateSystemConfigRequest {
    @NotBlank
    private String configValue;
}