package com.tamarcado.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuração de cache para testes: usa in-memory em vez de Redis.
 * Evita dependência do Redis durante os testes.
 */
@Configuration
@EnableCaching
@Profile("test")
public class TestCacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
                "geocoding:coordinates",
                "geocoding:address",
                "serviceSearch",
                "professionalDetail",
                "professionalDashboard",
                "clientDashboard"
        );
    }
}
