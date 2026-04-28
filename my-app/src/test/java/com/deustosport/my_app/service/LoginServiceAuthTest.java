package com.deustosport.my_app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deustosport.my_app.dto.CambioPasswordRequest;
import com.deustosport.my_app.dto.LoginRequest;
import com.deustosport.my_app.dto.LoginResponse;
import com.deustosport.my_app.dto.RegistroRequest;
import com.deustosport.my_app.entity.Credencial;
import com.deustosport.my_app.entity.Usuario;
import com.deustosport.my_app.enums.Rol;
import com.deustosport.my_app.repository.CredencialRepository;
import com.deustosport.my_app.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class LoginServiceAuthTest {

    private static final Logger log = LoggerFactory.getLogger(LoginServiceAuthTest.class);

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CredencialRepository credencialRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private LoginService loginService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void registrarUsuario_emailDuplicado_rechazaRegistro() {
        log.info("[TEST] registrarUsuario_emailDuplicado_rechazaRegistro");
        RegistroRequest request = new RegistroRequest(
                "Ana Lopez",
                "ana@deustosport.com",
                "12345678A",
                LocalDate.of(1990, 1, 1),
                "Secreto123",
                "+34666111222");

        when(usuarioRepository.existsByEmail("ana@deustosport.com")).thenReturn(true);

        LoginResponse response = loginService.registrarUsuario(request);

        assertFalse(response.isExitoso());
        assertTrue(response.getMensaje().contains("email ya está registrado"));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void registrarUsuario_exitoso_normalizaDatosYFuerzaRolCliente() {
        RegistroRequest request = new RegistroRequest(
                "  Maria Ruiz  ",
                "  MARIA@DEUSTOSPORT.COM ",
                "12345678z",
                LocalDate.of(1990, 1, 1),
                "PasswordSegura1",
                " 666111222 ");

        when(usuarioRepository.existsByEmail("maria@deustosport.com")).thenReturn(false);
        when(usuarioRepository.existsByDni("12345678Z")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            u.setId(40L);
            return u;
        });
        when(credencialRepository.save(any(Credencial.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LoginResponse response = loginService.registrarUsuario(request);

        assertTrue(response.isExitoso());
        assertEquals(40L, response.getUsuarioId());
        assertEquals("CLIENTE", response.getRol());

        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(usuarioCaptor.capture());
        Usuario guardado = usuarioCaptor.getValue();
        assertEquals("maria@deustosport.com", guardado.getEmail());
        assertEquals("12345678Z", guardado.getDni());
        assertEquals(Rol.CLIENTE, guardado.getRol());

        ArgumentCaptor<Credencial> credencialCaptor = ArgumentCaptor.forClass(Credencial.class);
        verify(credencialRepository).save(credencialCaptor.capture());
        assertTrue(encoder.matches("PasswordSegura1", credencialCaptor.getValue().getPasswordHash()));
    }

    @Test
    void iniciarSesion_passwordIncorrecta_devuelveError() {
        Usuario usuario = usuarioBase(10L, "ana@deustosport.com");
        Credencial credencial = new Credencial();
        credencial.setUsuario(usuario);
        credencial.setActivo(true);
        credencial.setPasswordHash(encoder.encode("Correcta123"));

        when(usuarioRepository.findByEmail("ana@deustosport.com")).thenReturn(Optional.of(usuario));
        when(credencialRepository.findByUsuarioId(10L)).thenReturn(Optional.of(credencial));

        LoginResponse response = loginService.iniciarSesion(new LoginRequest("ana@deustosport.com", "Mala123"));

        assertFalse(response.isExitoso());
        assertTrue(response.getMensaje().contains("incorrectos"));
    }

    @Test
    void iniciarSesion_exitoso_actualizaUltimoAcceso() {
        Usuario usuario = usuarioBase(20L, "luis@deustosport.com");
        Credencial credencial = new Credencial();
        credencial.setUsuario(usuario);
        credencial.setActivo(true);
        credencial.setPasswordHash(encoder.encode("Secreta123"));

        when(usuarioRepository.findByEmail("luis@deustosport.com")).thenReturn(Optional.of(usuario));
        when(credencialRepository.findByUsuarioId(20L)).thenReturn(Optional.of(credencial));
        when(credencialRepository.save(any(Credencial.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LoginResponse response = loginService.iniciarSesion(new LoginRequest("luis@deustosport.com", "Secreta123"));

        assertTrue(response.isExitoso());
        assertEquals("CLIENTE", response.getRol());

        ArgumentCaptor<Credencial> credencialCaptor = ArgumentCaptor.forClass(Credencial.class);
        verify(credencialRepository).save(credencialCaptor.capture());
        assertNotNull(credencialCaptor.getValue().getUltimoAcceso());
    }

    @Test
    void solicitarRecuperacion_emailNoExiste_respuestaGenericaExitosa() {
        when(usuarioRepository.findByEmail("nadie@deustosport.com")).thenReturn(Optional.empty());

        LoginResponse response = loginService.solicitarRecuperacion("nadie@deustosport.com");

        assertTrue(response.isExitoso());
        assertTrue(response.getMensaje().contains("Si el email existe"));
        verify(emailService, never()).enviarEmailRecuperacion(any(), any());
    }

    @Test
    void restablecerPassword_tokenExpirado_devuelveError() {
        Usuario usuario = usuarioBase(30L, "carlos@deustosport.com");
        Credencial credencial = new Credencial();
        credencial.setUsuario(usuario);
        credencial.setTokenRecuperacion("TOKEN-123");
        credencial.setFechaExpiracionToken(LocalDateTime.now().minusMinutes(1));

        when(credencialRepository.findByTokenRecuperacion("TOKEN-123"))
                .thenReturn(Optional.of(credencial));

        LoginResponse response = loginService.restablecerPassword(new CambioPasswordRequest("TOKEN-123", "NuevaPass9"));

        assertFalse(response.isExitoso());
        assertTrue(response.getMensaje().contains("Token expirado"));
    }

    @Test
    void cambiarPassword_exitoso_reemplazaHash() {
        Usuario usuario = usuarioBase(50L, "eva@deustosport.com");

        Credencial credencial = new Credencial();
        credencial.setUsuario(usuario);
        credencial.setActivo(true);
        credencial.setPasswordHash(encoder.encode("Antigua123"));

        when(usuarioRepository.findById(50L)).thenReturn(Optional.of(usuario));
        when(credencialRepository.findByUsuarioId(50L)).thenReturn(Optional.of(credencial));
        when(credencialRepository.save(any(Credencial.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LoginResponse response = loginService.cambiarPassword(50L, "Antigua123", "Nueva12345");

        assertTrue(response.isExitoso());

        ArgumentCaptor<Credencial> captor = ArgumentCaptor.forClass(Credencial.class);
        verify(credencialRepository).save(captor.capture());
        assertTrue(encoder.matches("Nueva12345", captor.getValue().getPasswordHash()));
    }

    private Usuario usuarioBase(Long id, String email) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNombreCompleto("Usuario Prueba");
        usuario.setEmail(email);
        usuario.setRol(Rol.CLIENTE);
        usuario.setActivo(true);
        usuario.setEsSocio(false);
        usuario.setBilletera(new BigDecimal("25.00"));
        return usuario;
    }
}
