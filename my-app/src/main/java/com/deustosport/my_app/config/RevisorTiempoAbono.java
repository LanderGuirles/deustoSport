package com.deustosport.my_app.config;

import com.deustosport.my_app.service.AbonoUsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class RevisorTiempoAbono {

    private static final Logger log = LoggerFactory.getLogger(RevisorTiempoAbono.class);

    @Autowired
    private AbonoUsuarioService abonoUsuarioService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void revisarCaducidadAbonos() {
        int caducados = abonoUsuarioService.desactivarAbonosCaducados();
        log.info("CRON abonos: {} abonos caducados desactivados.", caducados);
    }
}