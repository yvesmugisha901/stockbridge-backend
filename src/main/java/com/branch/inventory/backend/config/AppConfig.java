package com.branch.inventory.backend.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * General application configuration.
 * Provides shared beans used across the service layer.
 */
@Configuration
public class AppConfig {

    /**
     * ModelMapper — maps between entities and DTOs throughout the service layer.
     * STRICT matching avoids accidental field mapping between similarly named
     * but semantically different properties.
     */
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setSkipNullEnabled(true); // don't overwrite fields with null on partial updates
        return mapper;
    }
}