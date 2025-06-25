package com.copypoint.api.domain.pathcontext;

import lombok.Data;

@Data
public class PathContext {
    private Long storeId;
    private Long copypointId;


    public boolean hasContext() {
        return storeId != null || copypointId != null;
    }

    public static PathContext extractPathContext(String path) {
        PathContext context = new PathContext();

        // Extraer storeId del path como /api/store/{storeId}/...
        if (path.contains("/stores/")) {
            try {
                String[] parts = path.split("/");
                for (int i = 0; i < parts.length - 1; i++) {
                    if ("stores".equals(parts[i]) && i + 1 < parts.length) {
                        context.setStoreId(Long.parseLong(parts[i + 1]));
                        break;
                    }
                }
            } catch (NumberFormatException e) {
                // Ignorar si no es un número válido
            }
        }

        // Extraer copypointId del path como /api/store/{storeId}/copypoint/{copypointId}/...
        if (path.contains("/copypoints/")) {
            try {
                String[] parts = path.split("/");
                for (int i = 0; i < parts.length - 1; i++) {
                    if ("copypoints".equals(parts[i]) && i + 1 < parts.length) {
                        context.setCopypointId(Long.parseLong(parts[i + 1]));
                        break;
                    }
                }
            } catch (NumberFormatException e) {
                // Ignorar si no es un número válido
            }
        }

        return context;
    }

}
