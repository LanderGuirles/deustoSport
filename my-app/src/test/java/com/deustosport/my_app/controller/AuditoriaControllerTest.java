package com.deustosport.my_app.controller;

import com.deustosport.my_app.entity.Auditoria;
import com.deustosport.my_app.service.AuditoriaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuditoriaController.class)
@WithMockUser(roles = "ADMIN")
class AuditoriaControllerTest {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaControllerTest.class);

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditoriaService auditoriaService;

    @Test
    void obtenerAuditoriasRecientes_debeRetornarLista() throws Exception {
        log.info("[TEST] obtenerAuditoriasRecientes - debe retornar lista con 2 elementos");
        // Given
        List<Auditoria> auditorias = Arrays.asList(
            crearAuditoriaMock(1L, "user1", "LOGIN"),
            crearAuditoriaMock(2L, "user2", "RESERVA")
        );

        when(auditoriaService.obtenerAccionesRecientes(10)).thenReturn(auditorias);

        // When & Then
        mockMvc.perform(get("/api/auditoria")
                .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].usuario").value("user1"))
                .andExpect(jsonPath("$[1].usuario").value("user2"));
    }

    @Test
    void obtenerAuditoriasPorUsuario_debeRetornarListaFiltrada() throws Exception {
        // Given
        String usuario = "testUser";
        List<Auditoria> auditorias = Arrays.asList(
            crearAuditoriaMock(1L, usuario, "LOGIN"),
            crearAuditoriaMock(2L, usuario, "RESERVA")
        );

        when(auditoriaService.obtenerAccionesPorUsuario(usuario)).thenReturn(auditorias);

        // When & Then
        mockMvc.perform(get("/api/auditoria/usuario/{usuario}", usuario))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].usuario").value(usuario))
                .andExpect(jsonPath("$[1].usuario").value(usuario));
    }

    @Test
    void obtenerAuditoriasPorAccion_debeRetornarListaFiltrada() throws Exception {
        // Given
        String entidad = "Usuario";
        List<Auditoria> auditorias = Arrays.asList(
            crearAuditoriaMock(1L, "user1", "LOGIN"),
            crearAuditoriaMock(2L, "user2", "LOGIN")
        );

        when(auditoriaService.obtenerAccionesPorEntidad(entidad)).thenReturn(auditorias);

        // When & Then
        mockMvc.perform(get("/api/auditoria/entidad/{entidad}", entidad))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void obtenerAuditoriasPorRangoFechas_debeRetornarListaFiltrada() throws Exception {
        // Given
        String fechaInicio = "2024-01-01T00:00:00";
        String fechaFin = "2024-01-31T23:59:59";
        List<Auditoria> auditorias = Arrays.asList(
            crearAuditoriaMock(1L, "user1", "LOGIN"),
            crearAuditoriaMock(2L, "user2", "RESERVA")
        );

        when(auditoriaService.obtenerAccionesPorFecha(any(), any())).thenReturn(auditorias);

        // When & Then
        mockMvc.perform(get("/api/auditoria/fecha")
                .param("fechaInicio", fechaInicio)
                .param("fechaFin", fechaFin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    private Auditoria crearAuditoriaMock(Long id, String usuario, String accion) {
        Auditoria auditoria = new Auditoria();
        auditoria.setId(id);
        auditoria.setUsuario(usuario);
        auditoria.setAccion(accion);
        auditoria.setDetalles("Test details");
        auditoria.setFechaHora(LocalDateTime.now());
        return auditoria;
    }
}