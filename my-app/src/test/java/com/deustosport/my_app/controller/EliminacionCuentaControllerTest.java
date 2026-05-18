package com.deustosport.my_app.controller;

import com.deustosport.my_app.dto.SolicitudEliminacionCuentaRequest;
import com.deustosport.my_app.entity.Usuario;
import com.deustosport.my_app.enums.Rol;
import com.deustosport.my_app.service.UsuarioService;
import com.deustosport.my_app.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitarios para EliminacionCuentaController.
 * Valida el flujo completo de eliminación de cuentas.
 */
@WebMvcTest(EliminacionCuentaController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(roles = "CLIENTE")
class EliminacionCuentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Usuario usuarioTest;
    private Long usuarioTestId;

    @BeforeEach
    void setUp() {
        // Configurar usuario de prueba
        usuarioTestId = 1L;
        usuarioTest = new Usuario();
        usuarioTest.setId(usuarioTestId);
        usuarioTest.setNombreCompleto("Juan Pérez García");
        usuarioTest.setEmail("juan@example.com");
        usuarioTest.setDni("12345678A");
        usuarioTest.setRol(Rol.CLIENTE);
        usuarioTest.setActivo(true);
        usuarioTest.setEsSocio(false);
        usuarioTest.setBilletera(BigDecimal.ZERO);
        usuarioTest.setFechaNacimiento(LocalDate.of(1990, 5, 15));
        usuarioTest.setTelefono("123456789");
    }

    /**
     * Test: Validar que una cuenta puede ser eliminada sin problemas
     */
    @Test
    void testValidarEliminacion_PuedeSer_Exitoso() throws Exception {
        // Given
        when(usuarioRepository.findById(usuarioTestId)).thenReturn(Optional.of(usuarioTest));
        when(usuarioService.puedeSerEliminado(usuarioTestId)).thenReturn(true);
        when(usuarioService.obtenerMotivoNoEliminable(usuarioTestId)).thenReturn("");

        // When & Then
        mockMvc.perform(get("/api/cuenta/validar-eliminacion")
                .header("X-Usuario-Id", usuarioTestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.puedeSerEliminado").value(true))
                .andExpect(jsonPath("$.usuario").value("juan@example.com"))
                .andExpect(jsonPath("$.nombre").value("Juan Pérez García"))
                .andExpect(jsonPath("$.mensaje").value("Tu cuenta puede ser eliminada sin problemas"));

        verify(usuarioRepository).findById(usuarioTestId);
        verify(usuarioService).puedeSerEliminado(usuarioTestId);
    }

    /**
     * Test: Validar que una cuenta NO puede ser eliminada por tener saldo en billetera
     */
    @Test
    void testValidarEliminacion_NoCanBeSinceBilletera() throws Exception {
        // Given
        usuarioTest.setBilletera(new BigDecimal("50.00"));
        when(usuarioRepository.findById(usuarioTestId)).thenReturn(Optional.of(usuarioTest));
        when(usuarioService.puedeSerEliminado(usuarioTestId)).thenReturn(false);
        when(usuarioService.obtenerMotivoNoEliminable(usuarioTestId))
                .thenReturn("- Tiene un saldo pendiente en la billetera (50.00€). ");

        // When & Then
        mockMvc.perform(get("/api/cuenta/validar-eliminacion")
                .header("X-Usuario-Id", usuarioTestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.puedeSerEliminado").value(false))
                .andExpect(jsonPath("$.motivos").isNotEmpty());

        verify(usuarioRepository).findById(usuarioTestId);
        verify(usuarioService).puedeSerEliminado(usuarioTestId);
    }

    /**
     * Test: Obtener información sobre la eliminación
     */
    @Test
    void testObtenerInfoEliminacion_Exitoso() throws Exception {
        // Given
        when(usuarioRepository.findById(usuarioTestId)).thenReturn(Optional.of(usuarioTest));

        // When & Then
        mockMvc.perform(get("/api/cuenta/info-eliminacion")
                .header("X-Usuario-Id", usuarioTestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario").value("juan@example.com"))
                .andExpect(jsonPath("$.nombre").value("Juan Pérez García"))
                .andExpect(jsonPath("$.datosAEliminar").isNotEmpty())
                .andExpect(jsonPath("$.motivosDisponibles").isNotEmpty())
                .andExpect(jsonPath("$.avisoLegal").isNotEmpty());

        verify(usuarioRepository).findById(usuarioTestId);
    }

    /**
     * Test: Solicitar eliminación sin confirmación explícita
     */
    @Test
    void testSolicitarEliminacion_SinConfirmacion_Rechazada() throws Exception {
        // Given
        SolicitudEliminacionCuentaRequest solicitud = new SolicitudEliminacionCuentaRequest();
        solicitud.setContrasena("password123");
        solicitud.setConfirmacion(false); // No confirmado
        solicitud.setMotivo("NO_SATISFECHOS");

        when(usuarioRepository.findById(usuarioTestId)).thenReturn(Optional.of(usuarioTest));

        // When & Then
        mockMvc.perform(post("/api/cuenta/solicitar-eliminacion")
                .header("X-Usuario-Id", usuarioTestId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exitoso").value(false))
                .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("confirmar")));

        verify(usuarioRepository).findById(usuarioTestId);
    }

    /**
     * Test: Solicitar eliminación sin contraseña
     */
    @Test
    void testSolicitarEliminacion_SinContrasena_Rechazada() throws Exception {
        // Given
        SolicitudEliminacionCuentaRequest solicitud = new SolicitudEliminacionCuentaRequest();
        solicitud.setContrasena(""); // Sin contraseña
        solicitud.setConfirmacion(true);
        solicitud.setMotivo("NO_SATISFECHOS");

        when(usuarioRepository.findById(usuarioTestId)).thenReturn(Optional.of(usuarioTest));

        // When & Then
        mockMvc.perform(post("/api/cuenta/solicitar-eliminacion")
                .header("X-Usuario-Id", usuarioTestId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exitoso").value(false));
    }

    /**
     * Test: Solicitar eliminación sin motivo
     */
    @Test
    void testSolicitarEliminacion_SinMotivo_Rechazada() throws Exception {
        // Given
        SolicitudEliminacionCuentaRequest solicitud = new SolicitudEliminacionCuentaRequest();
        solicitud.setContrasena("password123");
        solicitud.setConfirmacion(true);
        solicitud.setMotivo(""); // Sin motivo

        when(usuarioRepository.findById(usuarioTestId)).thenReturn(Optional.of(usuarioTest));

        // When & Then
        mockMvc.perform(post("/api/cuenta/solicitar-eliminacion")
                .header("X-Usuario-Id", usuarioTestId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exitoso").value(false));
        
    }

    /**
     * Test: Solicitar eliminación cuando la cuenta no puede ser eliminada
     */
    @Test
    void testSolicitarEliminacion_CuentaNoEliminable_Rechazada() throws Exception {
        // Given
        SolicitudEliminacionCuentaRequest solicitud = new SolicitudEliminacionCuentaRequest();
        solicitud.setContrasena("password123");
        solicitud.setConfirmacion(true);
        solicitud.setMotivo("NO_SATISFECHOS");

        usuarioTest.setBilletera(new BigDecimal("100.00"));
        when(usuarioRepository.findById(usuarioTestId)).thenReturn(Optional.of(usuarioTest));
        when(usuarioService.puedeSerEliminado(usuarioTestId)).thenReturn(false);
        when(usuarioService.obtenerMotivoNoEliminable(usuarioTestId))
                .thenReturn("- Tiene saldo pendiente en billetera");

        // When & Then
        mockMvc.perform(post("/api/cuenta/solicitar-eliminacion")
                .header("X-Usuario-Id", usuarioTestId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.exitoso").value(false))
                .andExpect(jsonPath("$.detalles").value(org.hamcrest.Matchers.containsString("saldo")));

        verify(usuarioRepository).findById(usuarioTestId);
        verify(usuarioService).puedeSerEliminado(usuarioTestId);
    }

    /**
     * Test: Solicitar eliminación con contraseña incorrecta
     */
    @Test
    void testSolicitarEliminacion_ContraseniaIncorrecta_Rechazada() throws Exception {
        // Given
        SolicitudEliminacionCuentaRequest solicitud = new SolicitudEliminacionCuentaRequest();
        solicitud.setContrasena("passwordIncorrecto");
        solicitud.setConfirmacion(true);
        solicitud.setMotivo("NO_SATISFECHOS");

        when(usuarioRepository.findById(usuarioTestId)).thenReturn(Optional.of(usuarioTest));
        when(usuarioService.puedeSerEliminado(usuarioTestId)).thenReturn(true);
        when(usuarioService.solicitarEliminacionCuenta(anyLong(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("La contraseña es incorrecta. No se puede eliminar la cuenta sin verificación correcta."));

        // When & Then
        mockMvc.perform(post("/api/cuenta/solicitar-eliminacion")
                .header("X-Usuario-Id", usuarioTestId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.exitoso").value(false))
                .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("incorrecta")));

        verify(usuarioRepository).findById(usuarioTestId);
        verify(usuarioService).puedeSerEliminado(usuarioTestId);
    }

    /**
     * Test: Eliminar cuenta exitosamente
     */
    @Test
    void testSolicitarEliminacion_Exitoso() throws Exception {
        // Given
        SolicitudEliminacionCuentaRequest solicitud = new SolicitudEliminacionCuentaRequest();
        solicitud.setContrasena("password123");
        solicitud.setConfirmacion(true);
        solicitud.setMotivo("NO_SATISFECHOS");

        when(usuarioRepository.findById(usuarioTestId)).thenReturn(Optional.of(usuarioTest));
        when(usuarioService.puedeSerEliminado(usuarioTestId)).thenReturn(true);
        when(usuarioService.solicitarEliminacionCuenta(usuarioTestId, solicitud.getContrasena(), solicitud.getMotivo()))
                .thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/cuenta/solicitar-eliminacion")
                .header("X-Usuario-Id", usuarioTestId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("eliminada")))
                .andExpect(jsonPath("$.usuarioEmail").value("juan@example.com"))
                .andExpect(jsonPath("$.motivo").value("NO_SATISFECHOS"))
                .andExpect(jsonPath("$.fechaEliminacion").isNotEmpty());

        verify(usuarioRepository).findById(usuarioTestId);
        verify(usuarioService).puedeSerEliminado(usuarioTestId);
        verify(usuarioService).solicitarEliminacionCuenta(usuarioTestId, solicitud.getContrasena(), solicitud.getMotivo());
    }

    /**
     * Test: Cancelar solicitud de eliminación
     */
    @Test
    void testCancelarEliminacion_Exitoso() throws Exception {
        // Given
        when(usuarioRepository.findById(usuarioTestId)).thenReturn(Optional.of(usuarioTest));

        // When & Then
        mockMvc.perform(post("/api/cuenta/cancelar-eliminacion")
                .header("X-Usuario-Id", usuarioTestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("cancelada")))
                .andExpect(jsonPath("$.usuario").value("juan@example.com"));

        verify(usuarioRepository).findById(usuarioTestId);
    }

    /**
     * Test: Validar eliminación con usuario no encontrado
     */
    @Test
    void testValidarEliminacion_UsuarioNoEncontrado() throws Exception {
        // Given
        when(usuarioRepository.findById(usuarioTestId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/cuenta/validar-eliminacion")
                .header("X-Usuario-Id", usuarioTestId))
                .andExpect(status().isBadRequest());

        verify(usuarioRepository).findById(usuarioTestId);
    }

    /**
     * Test: Solicitar eliminación con usuario no encontrado
     */
    @Test
    void testSolicitarEliminacion_UsuarioNoEncontrado() throws Exception {
        // Given
        SolicitudEliminacionCuentaRequest solicitud = new SolicitudEliminacionCuentaRequest();
        solicitud.setContrasena("password123");
        solicitud.setConfirmacion(true);
        solicitud.setMotivo("NO_SATISFECHOS");

        when(usuarioRepository.findById(usuarioTestId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/api/cuenta/solicitar-eliminacion")
                .header("X-Usuario-Id", usuarioTestId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.exitoso").value(false));

        verify(usuarioRepository).findById(usuarioTestId);
    }
}
