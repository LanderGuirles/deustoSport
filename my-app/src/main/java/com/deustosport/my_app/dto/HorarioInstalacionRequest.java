package com.deustosport.my_app.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public class HorarioInstalacionRequest {

    @NotNull
    private LocalTime horaApertura;

    @NotNull
    private LocalTime horaCierre;

    public LocalTime getHoraApertura() {
        return horaApertura;
    }

    public void setHoraApertura(LocalTime horaApertura) {
        this.horaApertura = horaApertura;
    }

    public LocalTime getHoraCierre() {
        return horaCierre;
    }

    public void setHoraCierre(LocalTime horaCierre) {
        this.horaCierre = horaCierre;
    }
}