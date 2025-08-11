package com.copypoint.api.infra.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Objects;

/**
 * Configuración de cache para la aplicación
 * <p>
 * Se habilita el caching para mejorar el rendimiento, especialmente
 * para las consultas de tasas de cambio que no cambian frecuentemente.
 */
@Configuration
@EnableCaching
@EnableScheduling
public class CacheConfiguration {
    /**
     * Configura el gestor de cache usando ConcurrentMapCacheManager
     * para un cache en memoria simple y eficiente.
     * <p>
     * Para aplicaciones más complejas, se podría usar Redis Cache Manager
     * o EhCache para cache distribuido.
     */
    @Bean
    public CacheManager cacheManager() {
        // Configurar los nombres de cache que se utilizarán
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager("exchangeRates");

        // Permitir crear caches dinámicamente si se necesitan
        cacheManager.setAllowNullValues(false);

        return cacheManager;
    }

    /**
     * Limpia el cache de tasas de cambio cada 30 minutos
     * para asegurar que las tasas se mantengan relativamente actualizadas.
     * <p>
     * Las tasas de cambio pueden cambiar varias veces al día, por lo que
     * un cache de 30 minutos proporciona un buen balance entre rendimiento
     * y precisión de datos.
     */
    @Scheduled(fixedRate = 30 * 60 * 1000) // 30 minutos en milisegundos
    public void evictExchangeRatesCache() {
        CacheManager cacheManager = cacheManager();
        if (cacheManager.getCache("exchangeRates") != null) {
            Objects.requireNonNull(cacheManager.getCache("exchangeRates")).clear();
            // Log para monitoreo (usando System.out por simplicidad,
            // en un entorno real usarías el logger de la clase)
            System.out.println("Cache de tasas de cambio limpiado automáticamente: " +
                    java.time.LocalDateTime.now());
        }
    }

    /**
     * Limpia todos los caches manualmente si es necesario
     * Este método podría ser llamado desde un endpoint de administración
     */
    public void evictAllCaches() {
        CacheManager cacheManager = cacheManager();
        cacheManager.getCacheNames()
                .parallelStream()
                .forEach(cacheName -> cacheManager.getCache(cacheName).clear());

        System.out.println("Todos los caches limpiados manualmente: " +
                java.time.LocalDateTime.now());
    }
}
