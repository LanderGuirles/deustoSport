package com.deustosport.my_app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.deustosport.my_app.dto.ActualizarPerfilRequest;
import com.deustosport.my_app.dto.PerfilUsuarioResponse;
import com.deustosport.my_app.entity.Usuario;
import com.deustosport.my_app.enums.Rol;
import com.deustosport.my_app.repository.CredencialRepository;
import com.deustosport.my_app.repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class LoginServiceProfileTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CredencialRepository credencialRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private LoginService loginService;

    @Test
    void obtenerPerfil_devuelveDatosDelUsuario() {
        Usuario usuario = new Usuario();
        usuario.setId(7L);
        usuario.setNombreCompleto("Ana Lopez");
        usuario.setEmail("ana@deustosport.com");
        usuario.setTelefono("666111222");
        usuario.setRol(Rol.CLIENTE);

        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(usuario));

        PerfilUsuarioResponse response = loginService.obtenerPerfil(7L);

        assertTrue(response.isExitoso());
        assertEquals(7L, response.getUsuarioId());
        assertEquals("Ana Lopez", response.getNombreCompleto());
        assertEquals("666111222", response.getTelefono());
        assertEquals("CLIENTE", response.getRol());
    }

    @Test
    void actualizarPerfil_actualizaNombreYTelefono() {
        Usuario usuario = new Usuario();
        usuario.setId(9L);
        usuario.setNombreCompleto("Nombre Antiguo");
        usuario.setEmail("usuario@deustosport.com");
        usuario.setTelefono("600000000");
        usuario.setRol(Rol.CLIENTE);

        ActualizarPerfilRequest request = new ActualizarPerfilRequest("Nombre Nuevo", "+34666111222");

        when(usuarioRepository.findById(9L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PerfilUsuarioResponse response = loginService.actualizarPerfil(9L, request);

        assertTrue(response.isExitoso());
        assertEquals("Nombre Nuevo", response.getNombreCompleto());
        assertEquals("+34666111222", response.getTelefono());
    }

    @Test
    void actualizarPerfil_telefonoVacioSeGuardaComoNull() {
        Usuario usuario = new Usuario();
        usuario.setId(12L);
        usuario.setNombreCompleto("Marta Ruiz");
        usuario.setEmail("marta@deustosport.com");
        usuario.setTelefono("699888777");
        usuario.setRol(Rol.CLIENTE);

        ActualizarPerfilRequest request = new ActualizarPerfilRequest("Marta Ruiz", "   ");

        when(usuarioRepository.findById(12L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PerfilUsuarioResponse response = loginService.actualizarPerfil(12L, request);

        assertTrue(response.isExitoso());
        assertNull(response.getTelefono());
    }
}
