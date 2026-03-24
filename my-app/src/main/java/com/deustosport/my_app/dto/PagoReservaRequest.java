package com.deustosport.my_app.dto;

import com.deustosport.my_app.enums.MetodoPago;
import lombok.Data;

@Data
public class PagoReservaRequest {
    private Long usuarioId;
    private MetodoPago metodoPago;
    private String numeroTarjeta;
    private String titularTarjeta;
    private String caducidadTarjeta;
    private String cvv;
    private String telefonoBizum;
    private String iban;
}