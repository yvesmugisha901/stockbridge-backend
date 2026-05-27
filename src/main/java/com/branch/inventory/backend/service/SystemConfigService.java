package com.branch.inventory.backend.service;

import com.branch.inventory.backend.dto.request.UpdateSystemConfigRequest;
import com.branch.inventory.backend.dto.response.SystemConfigResponse;
import com.branch.inventory.backend.model.SystemConfig;
import com.branch.inventory.backend.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SystemConfigService {

    private final SystemConfigRepository repo;

    public List<SystemConfigResponse> getAll() {
        return repo.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public SystemConfigResponse update(Long id, UpdateSystemConfigRequest request) {
        SystemConfig config = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Config not found"));
        config.setConfigValue(request.getConfigValue());
        return toResponse(repo.save(config));
    }

    public String getValue(String key) {
        return repo.findByConfigKey(key)
                .map(SystemConfig::getConfigValue)
                .orElseThrow(() -> new RuntimeException("Config key not found: " + key));
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