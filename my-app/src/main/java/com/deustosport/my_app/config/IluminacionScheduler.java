package com.deustosport.my_app.config;

import com.deustosport.my_app.service.IluminacionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Scheduler que revisa el estado de iluminación de todas las pistas cada minuto.
 *
 * Ejecuta IluminacionService.revisarIluminacion() periódicamente para:
 * - Encender luces 5 minutos antes de reservas confirmadas.
 * - Apagar luces 5 minutos después de que terminen (si no hay reserva consecutiva).
 */
@Configuration
public class IluminacionScheduler {

    private static final Logger log = LoggerFactory.getLogger(IluminacionScheduler.class);

    @Autowired
    private IluminacionService iluminacionService;

    /**
     * Ejecuta la revisión de iluminación cada 60 segundos.
     */
    @Scheduled(fixedRate = 60000)
    public void revisarIluminacionPistas() {
        try {
            iluminacionService.revisarIluminacion();
        } catch (Exception e) {
            log.error("Error en la revisión automática de iluminación: {}", e.getMessage(), e);
        }
    }
}
