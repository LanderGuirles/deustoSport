package com.deustosport.my_app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.deustosport.my_app.entity.Instalacion;
import com.deustosport.my_app.entity.Pista;
import com.deustosport.my_app.entity.Reserva;
import com.deustosport.my_app.entity.Usuario;
import com.deustosport.my_app.enums.EstadoReserva;
import com.deustosport.my_app.repository.PistaRepository;
import com.deustosport.my_app.repository.ReservaRepository;
import com.deustosport.my_app.repository.UsuarioRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private PistaRepository pistaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    private ReservaService reservaService;

    private Instalacion instalacion;

    @BeforeEach
    void setUp() {
        // Inyectamos los mocks a través del constructor
        reservaService = new ReservaService(reservaRepository, pistaRepository, usuarioRepository);
        instalacion = new Instalacion();
        instalacion.setHoraApertura(LocalTime.of(8, 0));
        instalacion.setHoraCierre(LocalTime.of(22, 0));
    }

    @Test
    void testCrearReservaExito() {
        // Datos de prueba
        Long usuarioId = 1L;
        Long pistaId = 1L;
        LocalDate fecha = LocalDate.now().plusDays(1);
        LocalTime hora = LocalTime.of(10, 0);
        int duracion = 60;

        Usuario usuarioMock = new Usuario();
        usuarioMock.setId(usuarioId);

        Pista pistaMock = new Pista();
        pistaMock.setId(pistaId);
        pistaMock.setActiva(true);
        pistaMock.setInstalacion(instalacion);

        // Comportamiento de los mocks
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuarioMock));
        when(pistaRepository.findById(pistaId)).thenReturn(Optional.of(pistaMock));
        // No hay conflictos
        when(reservaRepository.findConflictingReservations(any(), any(), any(), any()))
            .thenReturn(Collections.emptyList());
        
        // Simular guardado (devuelve la misma reserva)
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Ejecutar
        Reserva resultado = reservaService.crearReserva(usuarioId, pistaId, fecha, hora, duracion);

        // Verificaciones
        assertNotNull(resultado);
        assertEquals(EstadoReserva.CONFIRMADA, resultado.getEstado());
        assertEquals(fecha, resultado.getFechaReserva());
        assertEquals(hora, resultado.getHoraInicio());
        assertEquals(hora.plusMinutes(duracion), resultado.getHoraFin());
        
        verify(reservaRepository).save(any(Reserva.class));
    }

    @Test
    void testCrearReservaPistaNoActiva() {
        Long usuarioId = 1L;
        Long pistaId = 1L;

        Usuario usuarioMock = new Usuario();
        Pista pistaMock = new Pista();
        pistaMock.setActiva(false); // Pista inactiva
        pistaMock.setInstalacion(instalacion);

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuarioMock));
        when(pistaRepository.findById(pistaId)).thenReturn(Optional.of(pistaMock));

        assertThrows(IllegalStateException.class, () -> {
            reservaService.crearReserva(usuarioId, pistaId, LocalDate.now(), LocalTime.of(10, 0), 60);
        });
    }

    @Test
    void testCrearReservaConflictoHorario() {
        Long usuarioId = 1L;
        Long pistaId = 1L;
        LocalDate fecha = LocalDate.now().plusDays(1);
        LocalTime hora = LocalTime.of(10, 0);

        Usuario usuarioMock = new Usuario();
        Pista pistaMock = new Pista();
        pistaMock.setActiva(true);
        pistaMock.setInstalacion(instalacion);

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuarioMock));
        when(pistaRepository.findById(pistaId)).thenReturn(Optional.of(pistaMock));
        
        // Simular que YA existe una reserva conflicto
        Reserva conflicto = new Reserva();
        when(reservaRepository.findConflictingReservations(any(), any(), any(), any()))
            .thenReturn(java.util.List.of(conflicto));

        assertThrows(IllegalStateException.class, () -> {
            reservaService.crearReserva(usuarioId, pistaId, fecha, hora, 60);
        });
    }

    @Test
    void testCrearReservaFueraDeHorarioGeneral() {
        Long usuarioId = 1L;
        Long pistaId = 1L;
        LocalDate fecha = LocalDate.now().plusDays(1);

        Usuario usuarioMock = new Usuario();
        usuarioMock.setId(usuarioId);

        Pista pistaMock = new Pista();
        pistaMock.setId(pistaId);
        pistaMock.setActiva(true);
        pistaMock.setInstalacion(instalacion);

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuarioMock));
        when(pistaRepository.findById(pistaId)).thenReturn(Optional.of(pistaMock));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            reservaService.crearReserva(usuarioId, pistaId, fecha, LocalTime.of(21, 30), 60));

        assertTrue(exception.getMessage().contains("horario general del polideportivo"));
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void testConsultarDisponibilidadFueraDeHorarioGeneral() {
        Long pistaId = 1L;

        Pista pistaMock = new Pista();
        pistaMock.setId(pistaId);
        pistaMock.setActiva(true);
        pistaMock.setInstalacion(instalacion);

        when(pistaRepository.findById(pistaId)).thenReturn(Optional.of(pistaMock));

        assertThrows(IllegalStateException.class, () ->
            reservaService.consultarDisponibilidad(
                pistaId,
                LocalDate.now().plusDays(1),
                LocalTime.of(7, 0),
                LocalTime.of(8, 0)
            ));

        verify(reservaRepository, never()).findConflictingReservations(any(), any(), any(), any());
    }
}