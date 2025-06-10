package com.copypoint.api.infra.http.enpointModule;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EndpointModuleMapper {
    private final Map<String, String> endpointToModule = Map.of(
            "/api/stores/*/materials", "MATERIALS_MANAGEMENT",
            "/api/stores/*/services", "SERVICES_MANAGEMENT",
            "/api/copypoints/*/sales", "SALES_MANAGEMENT",
            "/api/stores/*/administrators", "USER_MANAGEMENT"
            // etc...
    );

    public String getModuleForEndpoint(String endpoint) {
        return endpointToModule.get(endpoint);
    }
}
