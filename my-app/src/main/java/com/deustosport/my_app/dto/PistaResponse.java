package com.deustosport.my_app.dto;

import com.deustosport.my_app.enums.TipoDeporte;

import lombok.Data;

@Data
public class PistaResponse {
    private Long id;
    private String nombre;
    private TipoDeporte tipoDeporte;
    private Integer maxJugadores;
    private boolean activa;
    private Long instalacionId;
    private String instalacionNombre;
}
