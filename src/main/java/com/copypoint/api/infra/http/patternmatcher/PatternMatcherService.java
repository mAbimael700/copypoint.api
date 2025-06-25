package com.copypoint.api.infra.http.patternmatcher;

import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

@Service
public class PatternMatcherService {
    private final AntPathMatcher antPathMatcher;

    public PatternMatcherService() {
        this.antPathMatcher = new AntPathMatcher();
        // Configurar el separador de rutas
        this.antPathMatcher.setPathSeparator("/");
    }

    /**
     * Verifica si un endpoint coincide con un patrón usando AntPathMatcher
     * Soporta patrones como:
     * - /api/users/**
     * - /api/stores/*/

    public boolean matches(String pattern, String endpoint) {
        // Separar método y ruta si el patrón incluye método
        String patternPath = extractPath(pattern);
        String endpointPath = extractPath(endpoint);

        // Verificar método si está presente en el patrón
        if (hasMethod(pattern) && hasMethod(endpoint)) {
            String patternMethod = extractMethod(pattern);
            String endpointMethod = extractMethod(endpoint);

            if (!patternMethod.equals(endpointMethod) && !patternMethod.equals("*")) {
                return false;
            }
        }

        return antPathMatcher.match(patternPath, endpointPath);
    }

    /**
     * Extrae el método HTTP del patrón/endpoint
     */
    private String extractMethod(String pattern) {
        if (pattern.contains(":")) {
            return pattern.substring(0, pattern.indexOf(":"));
        }
        return "";
    }

    /**
     * Extrae la ruta del patrón/endpoint
     */
    private String extractPath(String pattern) {
        if (pattern.contains(":")) {
            return pattern.substring(pattern.indexOf(":") + 1);
        }
        return pattern;
    }

    /**
     * Verifica si el patrón/endpoint incluye método HTTP
     */
    private boolean hasMethod(String pattern) {
        return pattern.contains(":");
    }

    /**
     * Método de utilidad para extraer variables de la ruta
     */
    public boolean matchAndExtract(String pattern, String endpoint) {
        return antPathMatcher.match(pattern, endpoint);
    }
}
