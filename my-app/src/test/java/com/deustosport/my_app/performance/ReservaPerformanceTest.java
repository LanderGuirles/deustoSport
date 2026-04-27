package com.deustosport.my_app.performance;

import org.databene.contiperf.PerfTest;
import org.databene.contiperf.Required;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Tests de rendimiento con ContiPerf (JUnit 4).
 *
 * REQUISITO: el servidor debe estar arrancado en localhost:8080.
 * Ejecutar con: ./gradlew :my-app:test --tests "*.performance.*"
 *
 * Genera reportes en: build/contiperf-report/
 */
public class ReservaPerformanceTest {

    private static final Logger log = LoggerFactory.getLogger(ReservaPerformanceTest.class);
    private static final String BASE_URL = "http://localhost:8080";

    @Rule
    public ContiPerfRule contiPerfRule = new ContiPerfRule();

    private final RestTemplate restTemplate = new RestTemplate();

    @BeforeClass
    public static void verificarServidor() {
        log.info("[PERF] Comprobando disponibilidad del servidor en {}", BASE_URL);
        boolean disponible = false;
        try (Socket s = new Socket("localhost", 8080)) {
            disponible = true;
        } catch (Exception e) {
            log.warn("[PERF] Servidor no disponible en {}. Tests de rendimiento omitidos.", BASE_URL);
        }
        Assume.assumeTrue("Servidor no disponible en localhost:8080 — arranca el servidor antes de ejecutar los tests de rendimiento.", disponible);
        log.info("[PERF] Servidor disponible. Iniciando batería de tests de rendimiento.");
    }

    // ══════════════════════════════════════════════════════════
    //  TEST 1 — Throughput: GET /api/pistas
    //  100 invocaciones, 5 hilos concurrentes
    //  Requisito: throughput mínimo de 5 op/s, media < 1000 ms
    // ══════════════════════════════════════════════════════════

    @Test
    @PerfTest(invocations = 100, threads = 5)
    @Required(throughput = 5, average = 1000, max = 5000)
    public void testGetPistas_throughput() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                BASE_URL + "/api/pistas", String.class
            );
            log.debug("[PERF] GET /api/pistas → {}", response.getStatusCode());
        } catch (ResourceAccessException e) {
            log.error("[PERF] Servidor no disponible en {}. Arranca el servidor antes de ejecutar.", BASE_URL);
            throw e;
        }
    }

    // ══════════════════════════════════════════════════════════
    //  TEST 2 — Duration: GET /api/tarifas
    //  Ejecución continua durante 5 segundos, 3 hilos
    //  Requisito: media < 1500 ms
    // ══════════════════════════════════════════════════════════

    @Test
    @PerfTest(duration = 5000, threads = 3)
    @Required(average = 1500)
    public void testGetTarifas_duration() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                BASE_URL + "/api/tarifas", String.class
            );
            log.debug("[PERF] GET /api/tarifas → {}", response.getStatusCode());
        } catch (ResourceAccessException e) {
            log.error("[PERF] Servidor no disponible en {}.", BASE_URL);
            throw e;
        }
    }

    // ══════════════════════════════════════════════════════════
    //  TEST 3 — Login fallido (caso negativo)
    //  30 invocaciones, 2 hilos — espera 401 como respuesta normal
    // ══════════════════════════════════════════════════════════

    @Test
    @PerfTest(invocations = 30, threads = 2)
    @Required(max = 3000)
    public void testLogin_credencialesInvalidas_rendimiento() {
        Map<String, String> body = new HashMap<>();
        body.put("email", "noexiste@deustosport.com");
        body.put("password", "passwordErronea99");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(BASE_URL + "/api/auth/login", request, String.class);
            log.warn("[PERF] Login fallido esperaba 401 pero obtuvo 200");
        } catch (HttpClientErrorException e) {
            // 401 es la respuesta esperada para credenciales inválidas
            log.debug("[PERF] POST /api/auth/login (inválido) → {} (esperado)", e.getStatusCode());
        } catch (ResourceAccessException e) {
            log.error("[PERF] Servidor no disponible en {}.", BASE_URL);
            throw e;
        }
    }

    // ══════════════════════════════════════════════════════════
    //  TEST 4 — GET /api/instalaciones
    //  50 invocaciones, 4 hilos — throughput + concurrencia
    // ══════════════════════════════════════════════════════════

    @Test
    @PerfTest(invocations = 50, threads = 4)
    @Required(throughput = 3, average = 1200)
    public void testGetInstalaciones_concurrencia() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                BASE_URL + "/api/instalaciones", String.class
            );
            log.debug("[PERF] GET /api/instalaciones → {}", response.getStatusCode());
        } catch (ResourceAccessException e) {
            log.error("[PERF] Servidor no disponible en {}.", BASE_URL);
            throw e;
        }
    }
}
