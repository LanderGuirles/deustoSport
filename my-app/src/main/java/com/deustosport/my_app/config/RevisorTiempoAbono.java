package com.deustosport.my_app.config;

import com.deustosport.my_app.service.AbonoUsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class RevisorTiempoAbono {

    @Autowired
    private AbonoUsuarioService abonoUsuarioService;

    // Se ejecuta todos los días a las 00:00 AM
    @Scheduled(cron = "0 0 0 * * ?")
    public void revisarCaducidadAbonos() {
        int caducados = abonoUsuarioService.desactivarAbonosCaducados();
        System.out.println("CRON ABONOS: Se han desactivado " + caducados + " abonos caducados.");
    }
}