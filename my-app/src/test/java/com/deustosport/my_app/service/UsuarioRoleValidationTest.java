package com.deustosport.my_app.service;

import com.deustosport.my_app.entity.Polideportivo;
import com.deustosport.my_app.entity.Ayuntamiento;
import com.deustosport.my_app.entity.Usuario;
import com.deustosport.my_app.enums.Rol;
import com.deustosport.my_app.repository.AyuntamientoRepository;
import com.deustosport.my_app.repository.CredencialRepository;
import com.deustosport.my_app.repository.PolideportivoRepository;
import com.deustosport.my_app.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioRoleValidationTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CredencialRepository credencialRepository;

    @Mock
    private PolideportivoRepository polideportivoRepository;

    @Mock
    private AyuntamientoRepository ayuntamientoRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void registrarSecretaria_sinPolideportivo_debeLanzarExcepcion() {
        assertThrows(IllegalArgumentException.class, () ->
            usuarioService.registrarUsuario("12345678B", "Maria", "Lopez", "maria@test.com", "600000000", "pass123", false, Rol.SECRETARIA, null, null)
        );
    }

    @Test
    void registrarAyuntamiento_sinAyuntamiento_debeLanzarExcepcion() {
        assertThrows(IllegalArgumentException.class, () ->
            usuarioService.registrarUsuario("12345678C", "Ayt", "Bilbao", "ayto@test.com", "600000000", "pass123", false, Rol.AYUNTAMIENTO, null, null)
        );
    }

    @Test
    void registrarSecretaria_conPolideportivo_debeFuncionar() {
        Long polideportivoId = 1L;
        Polideportivo polideportivo = new Polideportivo();
        polideportivo.setId(polideportivoId);

        when(polideportivoRepository.findById(polideportivoId)).thenReturn(Optional.of(polideportivo));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArguments()[0]);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");

        Usuario result = usuarioService.registrarUsuario("12345678B", "Maria", "Lopez", "maria@test.com", "600000000", "pass123", false, Rol.SECRETARIA, polideportivoId, null);

        assertNotNull(result);
        assertEquals(Rol.SECRETARIA, result.getRol());
        assertEquals(polideportivo, result.getPolideportivo());
    }

    @Test
    void registrarAyuntamiento_conAyuntamiento_debeFuncionar() {
        Long ayuntamientoId = 1L;
        Ayuntamiento ayuntamiento = new Ayuntamiento();
        ayuntamiento.setId(ayuntamientoId);

        when(ayuntamientoRepository.findById(ayuntamientoId)).thenReturn(Optional.of(ayuntamiento));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArguments()[0]);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");

        Usuario result = usuarioService.registrarUsuario("12345678C", "Ayt", "Bilbao", "ayto@test.com", "600000000", "pass123", false, Rol.AYUNTAMIENTO, null, ayuntamientoId);

        assertNotNull(result);
        assertEquals(Rol.AYUNTAMIENTO, result.getRol());
        assertEquals(ayuntamiento, result.getAyuntamiento());
    }
}
