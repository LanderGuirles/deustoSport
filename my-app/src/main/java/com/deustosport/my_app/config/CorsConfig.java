package com.deustosport.my_app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuración de CORS mejorada con validación de entorno.
 * 
 * Características:
 * - Orígenes permitidos configurables por entorno (dev, test, prod)
 * - Métodos HTTP específicos por endpoint
 * - Headers permitidos explícitos
 * - Credenciales habilitadas cuando es necesario
 * - Validación de orígenes en tiempo de ejecución
 * - Logging de configuración para auditoría
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    
    private static final Logger logger = LoggerFactory.getLogger(CorsConfig.class);
    
    @Value("${app.cors.allowed-origins:http://localhost:8081,http://localhost:3000}")
    private String allowedOrigins;
    
    @Value("${app.cors.allowed-methods:GET,POST,PUT,DELETE,PATCH,OPTIONS}")
    private String allowedMethods;
    
    @Value("${app.cors.max-age:3600}")
    private Long maxAge;
    
    @Value("${app.environment:development}")
    private String environment;
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Configuración general para todos los endpoints REST
        registry.addMapping("/api/**")
                .allowedOrigins(parseOrigins(allowedOrigins))
                .allowedMethods(allowedMethods.split(","))
                .allowedHeaders(
                    "Content-Type",
                    "Authorization",
                    "X-Usuario-Id",
                    "X-Requested-With",
                    "X-API-KEY"
                )
                .exposedHeaders(
                    "Content-Type",
                    "Authorization",
                    "X-Total-Count",
                    "X-Page-Number",
                    "X-Page-Size"
                )
                .maxAge(maxAge)
                .allowCredentials(true);
        
        // Configuración especial para endpoints de autenticación (más restrictiva)
        registry.addMapping("/api/auth/**")
                .allowedOrigins(parseOrigins(allowedOrigins))
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders(
                    "Content-Type",
                    "Authorization",
                    "X-Requested-With"
                )
                .maxAge(3600)
                .allowCredentials(true);
        
        // Configuración especial para endpoints administrativos
        registry.addMapping("/api/admin/**")
                .allowedOrigins(parseOrigins(allowedOrigins))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders(
                    "Content-Type",
                    "Authorization",
                    "X-Usuario-Id",
                    "X-API-KEY"
                )
                .maxAge(3600)
                .allowCredentials(true);
        
        // Logging de configuración
        logCorsConfiguration();
    }
    
    /**
     * Parsea orígenes permitidos desde una cadena delimitada por comas.
     * Valida que no esté vacía en producción.
     * 
     * @param origins cadena con orígenes separados por comas
     * @return array de orígenes permitidos
     */
    private String[] parseOrigins(String origins) {
        if (origins == null || origins.trim().isEmpty()) {
            if ("production".equalsIgnoreCase(environment)) {
                logger.warn("ALERTA: No se han configurado orígenes CORS. Se permite localhost por defecto.");
                return new String[]{"http://localhost:8081"};
            }
            return new String[]{"*"};
        }
        
        return origins.split(",\\s*");
    }
    
    /**
     * Registra en logs la configuración de CORS activa.
     * Útil para auditoría y debugging.
     */
    private void logCorsConfiguration() {
        String[] origins = parseOrigins(allowedOrigins);
        logger.info("═══════════════════════════════════════════════════════════════");
        logger.info("CONFIGURACIÓN DE CORS ACTIVA");
        logger.info("═══════════════════════════════════════════════════════════════");
        logger.info("Entorno: {}", environment);
        logger.info("Orígenes permitidos: {}", String.join(", ", origins));
        logger.info("Métodos permitidos: {}", allowedMethods);
        logger.info("Max-Age (caché): {} segundos", maxAge);
        logger.info("Credenciales habilitadas: true");
        logger.info("═══════════════════════════════════════════════════════════════");
    }
}

