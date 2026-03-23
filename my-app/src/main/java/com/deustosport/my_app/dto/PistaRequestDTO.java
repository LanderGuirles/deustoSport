package com.deustosport.my_app.dto;

import com.deustosport.my_app.enums.TipoDeporte;
import lombok.Data;

@Data
public class PistaRequestDTO {
    private String nombre;
    private TipoDeporte tipoDeporte;
    private Long instalacionId;
    private Integer maxJugadores;
    private boolean activa = true;
}