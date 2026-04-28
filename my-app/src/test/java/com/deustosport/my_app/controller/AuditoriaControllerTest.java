package com.deustosport.my_app.controller;

import com.deustosport.my_app.entity.Auditoria;
import com.deustosport.my_app.service.AuditoriaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuditoriaController.class)
class AuditoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditoriaService auditoriaService;

    @Test
    void obtenerAuditoriasRecientes_debeRetornarLista() throws Exception {
        // Given
        List<Auditoria> auditorias = Arrays.asList(
            crearAuditoriaMock(1L, "user1", "LOGIN"),
            crearAuditoriaMock(2L, "user2", "RESERVA")
        );

        when(auditoriaService.obtenerAuditoriasRecientes(10)).thenReturn(auditorias);

        // When & Then
        mockMvc.perform(get("/api/auditoria/recientes")
                .param("limite", "10"))
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

        when(auditoriaService.obtenerAuditoriasPorUsuario(usuario)).thenReturn(auditorias);

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
        String accion = "LOGIN";
        List<Auditoria> auditorias = Arrays.asList(
            crearAuditoriaMock(1L, "user1", accion),
            crearAuditoriaMock(2L, "user2", accion)
        );

        when(auditoriaService.obtenerAuditoriasPorAccion(accion)).thenReturn(auditorias);

        // When & Then
        mockMvc.perform(get("/api/auditoria/accion/{accion}", accion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].accion").value(accion))
                .andExpect(jsonPath("$[1].accion").value(accion));
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

        when(auditoriaService.obtenerAuditoriasPorRangoFechas(any(), any())).thenReturn(auditorias);

        // When & Then
        mockMvc.perform(get("/api/auditoria/rango-fechas")
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
        auditoria.setIp("192.168.1.100");
        return auditoria;
    }
}