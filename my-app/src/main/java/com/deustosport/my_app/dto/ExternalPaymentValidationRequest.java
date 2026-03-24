package com.deustosport.my_app.dto;

import com.deustosport.my_app.enums.MetodoPago;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExternalPaymentValidationRequest {
    private String localReference;
    private BigDecimal amount;
    private String currency;
    private MetodoPago metodoPago;
    private String cardLast4;
    private String bizumPhone;
}