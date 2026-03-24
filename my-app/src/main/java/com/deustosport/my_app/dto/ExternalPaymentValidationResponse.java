package com.deustosport.my_app.dto;

import lombok.Data;

@Data
public class ExternalPaymentValidationResponse {
    private Boolean valid;
    private String gatewayReference;
    private String message;
}