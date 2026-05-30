package com.branch.inventory.backend.service;

import com.branch.inventory.backend.dto.request.UpdateSystemConfigRequest;
import com.branch.inventory.backend.dto.response.SystemConfigResponse;
import com.branch.inventory.backend.model.SystemConfig;
import com.branch.inventory.backend.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SystemConfigService {

    private final SystemConfigRepository repo;
    private final AuditLogService auditLogService;

    public List<SystemConfigResponse> getAll() {
        return repo.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public SystemConfigResponse update(Long id, UpdateSystemConfigRequest request) {
        SystemConfig config = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Config not found: " + id));

        String oldValue = config.getConfigValue();
        config.setConfigValue(request.getConfigValue());
        SystemConfig saved = repo.save(config);

        // Audit every config change (NFR-10)
        String actor = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        auditLogService.log(
                "UPDATE_CONFIG",
                "SystemConfig",
                id,
                actor,
                config.getConfigKey() + ": [" + oldValue + "] → [" + request.getConfigValue() + "]");

        return toResponse(saved);
    }

    /** Convenience accessor used by other services (e.g. approval routing). */
    public String getValue(String key) {
        return repo.findByConfigKey(key)
                .map(SystemConfig::getConfigValue)
                .orElseThrow(() -> new RuntimeException("Config key not found: " + key));
    }

    public long getLongValue(String key) {
        return Long.parseLong(getValue(key).trim());
    }

    public boolean getBooleanValue(String key) {
        return Boolean.parseBoolean(getValue(key).trim());
    }

    private SystemConfigResponse toResponse(SystemConfig c) {
        return SystemConfigResponse.builder()
                .id(c.getId())
                .configKey(c.getConfigKey())
                .configValue(c.getConfigValue())
                .description(c.getDescription())
                .build();
    }
}
