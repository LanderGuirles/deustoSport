package com.deustosport.my_app.controller;

import com.deustosport.my_app.dto.RegistroUsuarioRequest;
import com.deustosport.my_app.entity.Usuario;
import com.deustosport.my_app.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void registrarUsuario_debeRetornarUsuarioCreado() throws Exception {
        RegistroUsuarioRequest request = new RegistroUsuarioRequest();
        request.setDni("12345678A");
        request.setNombre("Juan");
        request.setApellidos("Pérez García");
        request.setEmail("juan@test.com");
        request.setTelefono("+34612345678");
        request.setPassword("password123");
        request.setEsSocio(true);

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Juan");
        usuario.setApellidos("Pérez García");
        usuario.setEmail("juan@test.com");
        usuario.setEsSocio(true);
        usuario.setBilletera(BigDecimal.ZERO);
        usuario.setActivo(true);

        when(usuarioService.registrarUsuario(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyBoolean()))
            .thenReturn(usuario);

        mockMvc.perform(post("/api/usuarios/registro")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Juan"))
                .andExpect(jsonPath("$.email").value("juan@test.com"));
    }

    @Test
    @WithMockUser(username = "1")
    void obtenerPerfil_debeRetornarUsuario() throws Exception {
        Long usuarioId = 1L;
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        usuario.setNombre("Juan");
        usuario.setEmail("juan@test.com");

        when(usuarioService.obtenerPorId(usuarioId)).thenReturn(java.util.Optional.of(usuario));

        mockMvc.perform(get("/api/usuarios/perfil"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Juan"));
    }

    @Test
    @WithMockUser(username = "1")
    void recargarBilletera_debeRetornarSaldoActualizado() throws Exception {
        Long usuarioId = 1L;

        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        usuario.setBilletera(new BigDecimal("50.00"));

        when(usuarioService.recargarBilletera(eq(usuarioId), any(BigDecimal.class))).thenReturn(usuario);

        Map<String, Object> body = Map.of("cantidad", 50.00);

        mockMvc.perform(post("/api/usuarios/recargar-billetera")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nuevoSaldo").value(50.00));
    }

    @Test
    @WithMockUser(username = "1")
    void cambiarPassword_debeRetornarOk() throws Exception {
        Long usuarioId = 1L;

        Map<String, String> body = Map.of("oldPassword", "oldPass99", "newPassword", "newPass123");

        mockMvc.perform(post("/api/usuarios/cambiar-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        verify(usuarioService).cambiarPassword(eq(usuarioId), eq("oldPass99"), eq("newPass123"));
    }
}