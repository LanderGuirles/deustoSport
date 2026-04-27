package com.deustosport.my_app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.deustosport.my_app.dto.ExternalPaymentValidationRequest;
import com.deustosport.my_app.dto.ExternalPaymentValidationResponse;
import com.deustosport.my_app.enums.MetodoPago;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class PaymentGatewayClientTest {

    private static final Logger log = LoggerFactory.getLogger(PaymentGatewayClientTest.class);

    @Mock
    private RestTemplate restTemplate;

    private PaymentGatewayClient paymentGatewayClient;

    @BeforeEach
    void setUp() {
        paymentGatewayClient = new PaymentGatewayClient(restTemplate);
        ReflectionTestUtils.setField(paymentGatewayClient, "paymentGatewayBaseUrl", "http://localhost:9090");
        ReflectionTestUtils.setField(paymentGatewayClient, "paymentGatewayValidatePath", "/api/payments/validate");
        ReflectionTestUtils.setField(paymentGatewayClient, "paymentGatewayApiKey", "api-test-key");
    }

    @Test
    void validarPagoExterno_pasarelaDeshabilitada_devuelveReferenciaLocal() {
        log.info("[TEST] pasarela deshabilitada → referencia local sin llamar a RestTemplate");
        ReflectionTestUtils.setField(paymentGatewayClient, "paymentGatewayEnabled", false);

        String referencia = paymentGatewayClient.validarPagoExterno(
                "LOCAL-1", new BigDecimal("12.00"), MetodoPago.TARJETA, "4111111111111111", null);

        assertEquals("LOCAL-1", referencia);
        verifyNoInteractions(restTemplate);
    }

    @Test
    void validarPagoExterno_respuestaValida_devuelveReferenciaPasarela() {
        log.info("[TEST] pasarela habilitada, respuesta válida → devuelve referencia GW-123");
        ReflectionTestUtils.setField(paymentGatewayClient, "paymentGatewayEnabled", true);

        ExternalPaymentValidationResponse body = new ExternalPaymentValidationResponse();
        body.setValid(true);
        body.setGatewayReference("GW-123");

        when(restTemplate.postForEntity(
                eq("http://localhost:9090/api/payments/validate"),
                any(HttpEntity.class),
                eq(ExternalPaymentValidationResponse.class)))
                .thenReturn(ResponseEntity.ok(body));

        String referencia = paymentGatewayClient.validarPagoExterno(
                "LOCAL-2", new BigDecimal("30.00"), MetodoPago.TARJETA, "4111 1111 1111 2222", null);

        assertEquals("GW-123", referencia);

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(
                eq("http://localhost:9090/api/payments/validate"),
                captor.capture(),
                eq(ExternalPaymentValidationResponse.class));

        HttpEntity captured = captor.getValue();
        ExternalPaymentValidationRequest request = (ExternalPaymentValidationRequest) captured.getBody();
        assertEquals("2222", request.getCardLast4());
        assertEquals("api-test-key", captured.getHeaders().getFirst("X-API-KEY"));
    }

    @Test
    void validarPagoExterno_rechazado_lanzaExcepcionConMensaje() {
        log.info("[TEST] pago rechazado por pasarela → debe lanzar IllegalStateException");
        ReflectionTestUtils.setField(paymentGatewayClient, "paymentGatewayEnabled", true);

        ExternalPaymentValidationResponse body = new ExternalPaymentValidationResponse();
        body.setValid(false);
        body.setMessage("Pago rechazado por riesgo");

        when(restTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(ExternalPaymentValidationResponse.class)))
                .thenReturn(ResponseEntity.ok(body));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> paymentGatewayClient.validarPagoExterno(
                        "LOCAL-3", new BigDecimal("30.00"), MetodoPago.BIZUM, null, "+34666111222"));

        assertTrue(ex.getMessage().contains("riesgo"));
    }

    @Test
    void validarPagoExterno_siRestTemplateFalla_lanzaIllegalStateException() {
        log.info("[TEST] RestTemplate lanza timeout → debe envolverse en IllegalStateException");
        ReflectionTestUtils.setField(paymentGatewayClient, "paymentGatewayEnabled", true);

        when(restTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(ExternalPaymentValidationResponse.class)))
                .thenThrow(new RestClientException("timeout"));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> paymentGatewayClient.validarPagoExterno(
                        "LOCAL-4", new BigDecimal("8.00"), MetodoPago.TARJETA, "4111111111111111", null));

        assertNotNull(ex.getCause());
    }

    private void assertNotNull(Object value) {
        assertTrue(value != null);
    }
}
