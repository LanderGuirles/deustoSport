package com.deustosport.my_app.controller;

import com.deustosport.my_app.dto.LoginRequestDTO;
import com.deustosport.my_app.dto.RegistroUsuarioDTO;
import com.deustosport.my_app.dto.UsuarioDTO;
import com.deustosport.my_app.entity.Usuario;
import com.deustosport.my_app.service.AuditoriaService;
import com.deustosport.my_app.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private AuditoriaService auditoriaService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registrarUsuario_debeRetornarUsuarioCreado() throws Exception {
        // Given
        RegistroUsuarioDTO request = new RegistroUsuarioDTO();
        request.setDni("12345678A");
        request.setNombre("Juan");
        request.setApellidos("Pérez García");
        request.setEmail("juan@test.com");
        request.setTelefono("+34612345678");
        request.setPassword("password123");
        request.setEsSocio(true);

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setDni("12345678A");
        usuario.setNombre("Juan");
        usuario.setApellidos("Pérez García");
        usuario.setEmail("juan@test.com");
        usuario.setTelefono("+34612345678");
        usuario.setEsSocio(true);
        usuario.setBilletera(BigDecimal.ZERO);
        usuario.setActivo(true);

        when(usuarioService.registrarUsuario(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyBoolean()))
            .thenReturn(usuario);

        // When & Then
        mockMvc.perform(post("/api/usuarios/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.dni").value("12345678A"))
                .andExpect(jsonPath("$.nombre").value("Juan"))
                .andExpect(jsonPath("$.email").value("juan@test.com"));

        verify(auditoriaService).registrarAccion(eq("SYSTEM"), eq("REGISTRO_USUARIO"), anyString(), anyString());
    }

    @Test
    void login_debeRetornarUsuarioLogueado() throws Exception {
        // Given
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("juan@test.com");
        request.setPassword("password123");

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("juan@test.com");
        usuario.setNombre("Juan");

        when(usuarioService.login(anyString(), anyString())).thenReturn(usuario);

        // When & Then
        mockMvc.perform(post("/api/usuarios/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("juan@test.com"));

        verify(auditoriaService).registrarAccion(eq("juan@test.com"), eq("LOGIN"), anyString(), anyString());
    }

    @Test
    void obtenerPerfil_debeRetornarUsuario() throws Exception {
        // Given
        Long usuarioId = 1L;
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        usuario.setNombre("Juan");
        usuario.setEmail("juan@test.com");

        when(usuarioService.obtenerUsuarioPorId(usuarioId)).thenReturn(usuario);

        // When & Then
        mockMvc.perform(get("/api/usuarios/{id}", usuarioId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Juan"));
    }

    @Test
    void recargarBilletera_debeRetornarUsuarioActualizado() throws Exception {
        // Given
        Long usuarioId = 1L;
        BigDecimal cantidad = new BigDecimal("50.00");

        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        usuario.setBilletera(new BigDecimal("50.00"));

        when(usuarioService.recargarBilletera(usuarioId, cantidad)).thenReturn(usuario);

        // When & Then
        mockMvc.perform(put("/api/usuarios/{id}/billetera", usuarioId)
                .param("cantidad", "50.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.billetera").value(50.00));

        verify(auditoriaService).registrarAccion(anyString(), eq("RECARGA_BILLETERA"), anyString(), anyString());
    }

    @Test
    void cambiarPassword_debeRetornarOk() throws Exception {
        // Given
        Long usuarioId = 1L;

        // When & Then
        mockMvc.perform(put("/api/usuarios/{id}/password", usuarioId)
                .param("oldPassword", "oldPass")
                .param("newPassword", "newPass"))
                .andExpect(status().isOk());

        verify(usuarioService).cambiarPassword(eq(usuarioId), eq("oldPass"), eq("newPass"));
        verify(auditoriaService).registrarAccion(anyString(), eq("CAMBIO_PASSWORD"), anyString(), anyString());
    }
}