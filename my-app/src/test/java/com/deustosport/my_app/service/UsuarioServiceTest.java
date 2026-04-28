package com.deustosport.my_app.service;

import com.deustosport.my_app.entity.Credencial;
import com.deustosport.my_app.entity.Usuario;
import com.deustosport.my_app.repository.CredencialRepository;
import com.deustosport.my_app.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    private static final Logger log = LoggerFactory.getLogger(UsuarioServiceTest.class);

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CredencialRepository credencialRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void registrarUsuario_debeCrearUsuarioCorrectamente() {
        log.info("[TEST] registrarUsuario - creación correcta de usuario socio");
        // Given
        String dni = "12345678A";
        String nombre = "Juan";
        String apellidos = "Pérez García";
        String email = "juan@test.com";
        String telefono = "+34612345678";
        String password = "password123";
        boolean esSocio = true;

        Usuario usuarioMock = new Usuario();
        usuarioMock.setId(1L);
        usuarioMock.setDni(dni);
        usuarioMock.setNombre(nombre);
        usuarioMock.setApellidos(apellidos);
        usuarioMock.setEmail(email);
        usuarioMock.setTelefono(telefono);
        usuarioMock.setEsSocio(esSocio);
        usuarioMock.setBilletera(BigDecimal.ZERO);
        usuarioMock.setActivo(true);

        when(usuarioRepository.existsByDni(dni)).thenReturn(false);
        when(usuarioRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioMock);

        // When
        Usuario result = usuarioService.registrarUsuario(dni, nombre, apellidos, email, telefono, password, esSocio);

        // Then
        assertNotNull(result);
        assertEquals(dni, result.getDni());
        assertEquals(nombre, result.getNombre());
        assertEquals(email, result.getEmail());
        assertTrue(result.isEsSocio());
        assertEquals(BigDecimal.ZERO, result.getBilletera());

        verify(emailService).enviarEmailBienvenida(email, nombre + " " + apellidos);
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void registrarUsuario_conDniDuplicado_debeLanzarExcepcion() {
        // Given
        String dni = "12345678A";
        when(usuarioRepository.existsByDni(dni)).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            usuarioService.registrarUsuario(dni, "Juan", "Pérez", "juan@test.com", "+34612345678", "pass", false));

        assertEquals("Ya existe un usuario con ese DNI", exception.getMessage());
    }

    @Test
    void registrarUsuario_conEmailDuplicado_debeLanzarExcepcion() {
        // Given
        String email = "juan@test.com";
        when(usuarioRepository.existsByDni("12345678A")).thenReturn(false);
        when(usuarioRepository.existsByEmail(email)).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            usuarioService.registrarUsuario("12345678A", "Juan", "Pérez", email, "+34612345678", "pass", false));

        assertEquals("Ya existe un usuario con ese email", exception.getMessage());
    }

    @Test
    void recargarBilletera_debeActualizarSaldo() {
        // Given
        Long usuarioId = 1L;
        BigDecimal cantidad = new BigDecimal("50.00");

        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        usuario.setBilletera(new BigDecimal("25.00"));

        Usuario usuarioActualizado = new Usuario();
        usuarioActualizado.setId(usuarioId);
        usuarioActualizado.setBilletera(new BigDecimal("75.00"));

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioActualizado);

        // When
        Usuario result = usuarioService.recargarBilletera(usuarioId, cantidad);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("75.00"), result.getBilletera());
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void cambiarPassword_debeActualizarPassword() {
        // Given
        Long usuarioId = 1L;
        String oldPassword = "oldPass";
        String newPassword = "newPass";

        Credencial credencial = new Credencial();
        credencial.setPasswordHash("encodedOldPass");

        when(credencialRepository.findByUsuarioId(usuarioId)).thenReturn(Optional.of(credencial));
        when(passwordEncoder.matches(oldPassword, "encodedOldPass")).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPass");

        // When
        usuarioService.cambiarPassword(usuarioId, oldPassword, newPassword);

        // Then
        verify(passwordEncoder).encode(newPassword);
        verify(credencialRepository).save(any(Credencial.class));
    }

    @Test
    void cambiarPassword_conPasswordIncorrecta_debeLanzarExcepcion() {
        // Given
        Long usuarioId = 1L;
        Credencial credencial = new Credencial();
        credencial.setPasswordHash("encodedPass");

        when(credencialRepository.findByUsuarioId(usuarioId)).thenReturn(Optional.of(credencial));
        when(passwordEncoder.matches("wrongPass", "encodedPass")).thenReturn(false);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            usuarioService.cambiarPassword(usuarioId, "wrongPass", "newPass"));

        assertEquals("La contraseña actual es incorrecta", exception.getMessage());
    }
}