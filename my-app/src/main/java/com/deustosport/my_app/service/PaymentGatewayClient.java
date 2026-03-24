package com.deustosport.my_app.service;

import com.deustosport.my_app.dto.ExternalPaymentValidationRequest;
import com.deustosport.my_app.dto.ExternalPaymentValidationResponse;
import com.deustosport.my_app.enums.MetodoPago;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class PaymentGatewayClient {

    private final RestTemplate restTemplate;

    @Value("${payment.gateway.base-url:http://localhost:9090}")
    private String paymentGatewayBaseUrl;

    @Value("${payment.gateway.validate-path:/api/payments/validate}")
    private String paymentGatewayValidatePath;

    @Value("${payment.gateway.api-key:}")
    private String paymentGatewayApiKey;

    @Value("${payment.gateway.enabled:false}")
    private boolean paymentGatewayEnabled;

    public PaymentGatewayClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String validarPagoExterno(String referenciaLocal, BigDecimal amount, MetodoPago metodoPago,
            String numeroTarjeta, String telefonoBizum) {
        if (!paymentGatewayEnabled) {
            return referenciaLocal;
        }

        String url = paymentGatewayBaseUrl + paymentGatewayValidatePath;
        String cardLast4 = getLast4(numeroTarjeta);
        ExternalPaymentValidationRequest request = new ExternalPaymentValidationRequest(
                referenciaLocal,
                amount,
                "EUR",
            metodoPago,
                cardLast4,
                telefonoBizum
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (paymentGatewayApiKey != null && !paymentGatewayApiKey.isBlank()) {
            headers.set("X-API-KEY", paymentGatewayApiKey);
        }

        HttpEntity<ExternalPaymentValidationRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<ExternalPaymentValidationResponse> response = restTemplate.postForEntity(
                    url,
                    entity,
                    ExternalPaymentValidationResponse.class
            );

            ExternalPaymentValidationResponse body = response.getBody();
            if (!response.getStatusCode().is2xxSuccessful() || body == null || !Boolean.TRUE.equals(body.getValid())) {
                String message = body != null && body.getMessage() != null ? body.getMessage() : "Pago rechazado por pasarela externa.";
                throw new IllegalStateException(message);
            }

            if (body.getGatewayReference() != null && !body.getGatewayReference().isBlank()) {
                return body.getGatewayReference();
            }
            return referenciaLocal;
        } catch (RestClientException ex) {
            throw new IllegalStateException("No se pudo validar el pago con la pasarela externa.", ex);
        }
    }

    private String getLast4(String numeroTarjeta) {
        if (numeroTarjeta == null) {
            return null;
        }

        String clean = numeroTarjeta.replace(" ", "").trim();
        if (clean.length() < 4) {
            return clean;
        }
        return clean.substring(clean.length() - 4);
    }
}