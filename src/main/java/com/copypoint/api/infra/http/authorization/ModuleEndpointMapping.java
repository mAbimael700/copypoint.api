package com.copypoint.api.infra.http.authorization;

import com.copypoint.api.domain.modules.ModuleType;
import com.copypoint.api.infra.http.patternmatcher.PatternMatcherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ModuleEndpointMapping {
    private final PatternMatcherService patternMatcherService;
    private final Map<ModuleType, List<String>> moduleEndpoints;

    @Autowired
    public ModuleEndpointMapping(PatternMatcherService patternMatcherService) {
        this.patternMatcherService = patternMatcherService;
        this.moduleEndpoints = initializeEndpoints();
    }

    private Map<ModuleType, List<String>> initializeEndpoints() {
        Map<ModuleType, List<String>> endpoints = new HashMap<>();


        endpoints.put(ModuleType.COPYPOINT_MANAGEMENT, Arrays.asList(
                        "POST:/api/stores/*/copypoints",
                        "GET:/api/stores/*/copypoints",
                        "GET:/api/copypoints/*/sales",
                        "GET:/api/copypoints/*/sales/pending",
                        "POST:/api/copypoints/*/sales/*/profiles",
                        "PATCH:/api/copypoints/*/sales/*/status",
                        "PATCH:/api/copypoints/*/sales/*/hold",
                        "GET:/api/copypoints/*/services"
                )
        );

        endpoints.put(ModuleType.STORE_MANAGEMENT, Arrays.asList(
                "POST:/api/stores/*/services",
                "GET:/api/stores/*/services",
                "POST:/api/stores/*/profiles",
                "GET:/api/stores/*/profiles"
        ));


        return Collections.unmodifiableMap(endpoints);
    }

    /**
     * Obtiene todos los endpoints asociados a un módulo
     */
    public List<String> getEndpointsForModule(String moduleCode) {
        return moduleEndpoints.getOrDefault(moduleCode, Collections.emptyList());
    }

    public Set<String> getAllModules() {
        return moduleEndpoints.keySet().stream()
                .map(ModuleType::getCode)
                .collect(Collectors.toSet());
    }

    /**
     * Verifica si un endpoint pertenece a un módulo específico
     */
    public boolean isEndpointInModule(String moduleCode, String endpoint) {
        List<String> moduleEndpointPatterns = getEndpointsForModule(moduleCode);
        return moduleEndpointPatterns.stream()
                .anyMatch(pattern -> patternMatcherService.matches(pattern, endpoint));
    }

    /**
     * Encuentra el módulo al que pertenece un endpoint
     */
    public Optional<ModuleType> findModuleForEndpoint(String method, String path) {
        var endpoint = method + ":" + path;

        return moduleEndpoints.entrySet().stream()
                .filter(entry -> entry.getValue().stream()
                        .anyMatch(pattern -> {
                            boolean matches = patternMatcherService.matches(pattern, endpoint);
                            if (matches) {
                                System.out.println("✓ Coincide con patrón: " + pattern);
                            }
                            return matches;
                        }))
                .map(Map.Entry::getKey)
                .findFirst();
    }

    /**
     * Obtiene todos los endpoints del sistema
     */
    public Set<String> getAllEndpoints() {
        return moduleEndpoints.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toSet());
    }

    /**
     * Agrega endpoints adicionales a un módulo (útil para extensiones futuras)
     */
    public void addEndpointsToModule(ModuleType moduleCode, List<String> newEndpoints) {
        if (moduleEndpoints.containsKey(moduleCode)) {
            List<String> currentEndpoints = new ArrayList<>(moduleEndpoints.get(moduleCode));
            currentEndpoints.addAll(newEndpoints);
            ((Map<ModuleType, List<String>>) moduleEndpoints).put(moduleCode, currentEndpoints);
        }
    }
}
