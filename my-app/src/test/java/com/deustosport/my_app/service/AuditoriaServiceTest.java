package com.deustosport.my_app.service;

import com.deustosport.my_app.entity.Auditoria;
import com.deustosport.my_app.repository.AuditoriaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditoriaServiceTest {

    @Mock
    private AuditoriaRepository auditoriaRepository;

    @InjectMocks
    private AuditoriaService auditoriaService;

    @Test
    void registrarAccion_debeGuardarAuditoriaCorrectamente() {
        // Given
        String usuario = "admin";
        String accion = "LOGIN";
        String detalles = "Inicio de sesión exitoso";
        String ip = "192.168.1.100";

        Auditoria auditoriaMock = new Auditoria();
        auditoriaMock.setId(1L);
        auditoriaMock.setUsuario(usuario);
        auditoriaMock.setAccion(accion);
        auditoriaMock.setDetalles(detalles);
        auditoriaMock.setFechaHora(LocalDateTime.now());

        when(auditoriaRepository.save(any(Auditoria.class))).thenReturn(auditoriaMock);

        // When
        auditoriaService.registrarAccion(usuario, accion, "Sistema", null, detalles);

        // Then
        verify(auditoriaRepository).save(any(Auditoria.class));
    }

    @Test
    void obtenerAuditoriasRecientes_debeRetornarListaCorrecta() {
        // Given
        int limite = 10;
        List<Auditoria> auditoriasMock = Arrays.asList(
            crearAuditoriaMock(1L, "user1", "LOGIN", "Login exitoso"),
            crearAuditoriaMock(2L, "user2", "RESERVA", "Reserva creada")
        );

        when(auditoriaRepository.findTopByOrderByFechaHoraDesc(limite)).thenReturn(auditoriasMock);

        // When
        List<Auditoria> result = auditoriaService.obtenerAccionesRecientes(limite);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("user1", result.get(0).getUsuario());
        assertEquals("user2", result.get(1).getUsuario());
    }

    @Test
    void obtenerAuditoriasPorUsuario_debeRetornarListaFiltrada() {
        // Given
        String usuario = "testUser";
        List<Auditoria> auditoriasMock = Arrays.asList(
            crearAuditoriaMock(1L, usuario, "LOGIN", "Login"),
            crearAuditoriaMock(2L, usuario, "RESERVA", "Reserva")
        );

        when(auditoriaRepository.findByUsuarioOrderByFechaHoraDesc(usuario)).thenReturn(auditoriasMock);

        // When
        List<Auditoria> result = auditoriaService.obtenerAccionesPorUsuario(usuario);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(usuario, result.get(0).getUsuario());
        assertEquals(usuario, result.get(1).getUsuario());
    }

    @Test
    void obtenerAuditoriasPorAccion_debeRetornarListaFiltrada() {
        // Given
        String accion = "LOGIN";
        List<Auditoria> auditoriasMock = Arrays.asList(
            crearAuditoriaMock(1L, "user1", accion, "Login 1"),
            crearAuditoriaMock(2L, "user2", accion, "Login 2")
        );

        when(auditoriaRepository.findByAccionOrderByFechaHoraDesc(accion)).thenReturn(auditoriasMock);

        // When
        List<Auditoria> result = auditoriaService.obtenerAuditoriasPorAccion(accion);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(accion, result.get(0).getAccion());
        assertEquals(accion, result.get(1).getAccion());
    }

    @Test
    void obtenerAuditoriasPorRangoFechas_debeRetornarListaFiltrada() {
        // Given
        LocalDateTime fechaInicio = LocalDateTime.now().minusDays(7);
        LocalDateTime fechaFin = LocalDateTime.now();
        List<Auditoria> auditoriasMock = Arrays.asList(
            crearAuditoriaMock(1L, "user1", "LOGIN", "Login"),
            crearAuditoriaMock(2L, "user2", "RESERVA", "Reserva")
        );

        when(auditoriaRepository.findByFechaHoraBetweenOrderByFechaHoraDesc(fechaInicio, fechaFin))
            .thenReturn(auditoriasMock);

        // When
        List<Auditoria> result = auditoriaService.obtenerAccionesPorFecha(fechaInicio, fechaFin);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    private Auditoria crearAuditoriaMock(Long id, String usuario, String accion, String detalles) {
        Auditoria auditoria = new Auditoria();
        auditoria.setId(id);
        auditoria.setUsuario(usuario);
        auditoria.setAccion(accion);
        auditoria.setDetalles(detalles);
        auditoria.setFechaHora(LocalDateTime.now());
        return auditoria;
    }
}