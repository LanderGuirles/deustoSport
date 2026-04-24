package com.deustosport.my_app.dto;

import lombok.Data;

@Data
public class PagoReservaRequest {
    private Long usuarioId;
    private String metodoPago;
    private String numeroTarjeta;
    private String titularTarjeta;
    private String caducidadTarjeta;
    private String cvv;
    private String telefonoBizum;
    private String iban;
}