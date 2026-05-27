package com.branch.inventory.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SystemConfigResponse {
    private Long id;
    private String configKey;
    private String configValue;
    private String description;
}