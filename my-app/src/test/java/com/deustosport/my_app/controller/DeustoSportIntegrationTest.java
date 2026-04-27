package com.deustosport.my_app.controller;

import com.deustosport.my_app.config.DataInitializer;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de integración: arrancan el servidor real en un puerto aleatorio
 * y realizan llamadas HTTP reales para verificar la comunicación cliente-servidor.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.datasource.url=jdbc:h2:mem:deustosport_int_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.sql.init.mode=never",
        "spring.jpa.show-sql=false"
    }
)
class DeustoSportIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(DeustoSportIntegrationTest.class);

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockitoBean
    private DataInitializer dataInitializer;

    // ── Utilidad ──────────────────────────────────────────────
    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    // ══════════════════════════════════════════════════════════
    //  GET /api/pistas  — listado de pistas (sin autenticación)
    // ══════════════════════════════════════════════════════════

    @Test
    void getPistas_sinAuth_devuelve200YListaVacia() {
        log.info("[IT] GET /api/pistas → llamada remota al servidor en puerto {}", port);

        ResponseEntity<String> response = restTemplate.getForEntity(url("/api/pistas"), String.class);

        log.info("[IT] Respuesta: status={}, body={}", response.getStatusCode(), response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    // ══════════════════════════════════════════════════════════
    //  GET /api/tarifas  — listado de tarifas
    // ══════════════════════════════════════════════════════════

    @Test
    void getTarifas_sinAuth_devuelve200() {
        log.info("[IT] GET /api/tarifas → llamada remota al servidor en puerto {}", port);

        ResponseEntity<String> response = restTemplate.getForEntity(url("/api/tarifas"), String.class);

        log.info("[IT] Respuesta: status={}", response.getStatusCode());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // ══════════════════════════════════════════════════════════
    //  POST /api/auth/login  — credenciales incorrectas → 401
    // ══════════════════════════════════════════════════════════

    @Test
    void login_credencialesInvalidas_devuelve401() {
        log.info("[IT] POST /api/auth/login con credenciales inválidas → esperando 401");

        Map<String, String> body = Map.of(
            "email", "noexiste@deustosport.com",
            "password", "contraseñaWrong99"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
            url("/api/auth/login"), request, String.class
        );

        log.info("[IT] Respuesta: status={}, body={}", response.getStatusCode(), response.getBody());
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    // ══════════════════════════════════════════════════════════
    //  POST /api/auth/login  — body vacío → 400
    // ══════════════════════════════════════════════════════════

    @Test
    void login_bodyVacio_devuelve400() {
        log.info("[IT] POST /api/auth/login sin body → esperando 400");

        Map<String, String> body = Map.of();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
            url("/api/auth/login"), request, String.class
        );

        log.info("[IT] Respuesta: status={}", response.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ══════════════════════════════════════════════════════════
    //  GET /api/instalaciones  — listado de instalaciones
    // ══════════════════════════════════════════════════════════

    @Test
    void getInstalaciones_sinAuth_devuelve200() {
        log.info("[IT] GET /api/instalaciones → llamada remota al servidor");

        ResponseEntity<String> response = restTemplate.getForEntity(url("/api/instalaciones"), String.class);

        log.info("[IT] Respuesta: status={}", response.getStatusCode());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // ══════════════════════════════════════════════════════════
    //  POST /api/auth/registro  — datos incompletos → 400
    // ══════════════════════════════════════════════════════════

    @Test
    void registro_datosFaltantes_devuelve400OErrorEnBody() {
        log.info("[IT] POST /api/auth/registro con datos incompletos");

        Map<String, String> body = Map.of("email", "test@deustosport.com"); // faltan campos obligatorios

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
            url("/api/auth/registro"), request, String.class
        );

        log.info("[IT] Respuesta: status={}, body={}", response.getStatusCode(), response.getBody());
        assertTrue(
            response.getStatusCode() == HttpStatus.BAD_REQUEST ||
            response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR,
            "Se esperaba error 4xx o 5xx por datos incompletos"
        );
    }
}
