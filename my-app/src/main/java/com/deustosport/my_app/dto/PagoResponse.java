package com.deustosport.my_app.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.deustosport.my_app.enums.EstadoPago;
import com.deustosport.my_app.enums.MetodoPago;
import lombok.Data;

@Data
public class PagoResponse {
    private String referenciaPago;
    private BigDecimal importe;
    private MetodoPago metodoPago;
    private EstadoPago estadoPago;
    private String iban;
    private LocalDateTime fechaPago;
}
