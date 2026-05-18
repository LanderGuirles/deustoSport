package com.deustosport.my_app.controller;

import com.deustosport.my_app.dto.*;
import com.deustosport.my_app.entity.Usuario;
import com.deustosport.my_app.enums.Rol;
import com.deustosport.my_app.repository.UsuarioRepository;
import com.deustosport.my_app.service.LoginService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginControllerTest {

    private static final Logger log = LoggerFactory.getLogger(LoginControllerTest.class);

    @Mock
    private LoginService loginService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private LoginController loginController;

    // ── POST /api/auth/registro ───────────────────────────────────────────────

    @Test
    void registro_exitoso_devuelve201() {
        log.info("[TEST] registro - servicio exitoso → 201");
        LoginResponse ok = new LoginResponse(1L, "Juan García", "juan@test.com",
                "CLIENTE", true, BigDecimal.ZERO, "Registro exitoso", true);
        when(loginService.registrarUsuario(any())).thenReturn(ok);

        ResponseEntity<LoginResponse> resp = loginController.registro(
                new RegistroRequest("Juan García", "juan@test.com", "12345678A", LocalDate.of(1990, 1, 1), "password123", null));

        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        assertTrue(resp.getBody().isExitoso());
        log.info("[TEST] Status: {}", resp.getStatusCode());
    }

    @Test
    void registro_servicioFalla_devuelve400() {
        log.info("[TEST] registro - servicio falla → 400");
        LoginResponse fail = new LoginResponse(null, null, null, null,
                false, null, "Email ya registrado", false);
        when(loginService.registrarUsuario(any())).thenReturn(fail);

        ResponseEntity<LoginResponse> resp = loginController.registro(
                new RegistroRequest("Juan García", "juan@test.com", "12345678A", LocalDate.of(1990, 1, 1), "password123", null));

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertFalse(resp.getBody().isExitoso());
    }

    // ── POST /api/auth/login ──────────────────────────────────────────────────

    @Test
    void login_credencialesValidas_devuelve200() {
        log.info("[TEST] login - credenciales válidas → 200");
        LoginResponse ok = new LoginResponse(1L, "Juan García", "juan@test.com",
                "CLIENTE", true, BigDecimal.ZERO, "Login exitoso", true);
        when(loginService.iniciarSesion(any())).thenReturn(ok);

        ResponseEntity<LoginResponse> resp = loginController.login(
                new LoginRequest("juan@test.com", "password123"));

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertTrue(resp.getBody().isExitoso());
    }

    @Test
    void login_credencialesInvalidas_devuelve401() {
        log.info("[TEST] login - credenciales inválidas → 401");
        LoginResponse fail = new LoginResponse(null, null, null, null,
                false, null, "Credenciales incorrectas", false);
        when(loginService.iniciarSesion(any())).thenReturn(fail);

        ResponseEntity<LoginResponse> resp = loginController.login(
                new LoginRequest("wrong@test.com", "wrongpass"));

        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
        assertFalse(resp.getBody().isExitoso());
    }

    @Test
    void login_sinEmail_devuelve400() {
        log.info("[TEST] login - email nulo → 400");
        ResponseEntity<LoginResponse> resp = loginController.login(
                new LoginRequest(null, "password123"));

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    void login_sinPassword_devuelve400() {
        log.info("[TEST] login - password nula → 400");
        ResponseEntity<LoginResponse> resp = loginController.login(
                new LoginRequest("juan@test.com", null));

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    // ── POST /api/auth/login-guest ────────────────────────────────────────────

    @Test
    void loginGuest_usuarioExistente_devuelve200() {
        log.info("[TEST] loginGuest - usuario invitado ya existe → 200");
        Usuario guest = new Usuario();
        guest.setId(1L);
        guest.setNombreCompleto("No Socio");
        guest.setEmail("invitado@deustosport.com");
        guest.setEsSocio(false);
        guest.setBilletera(BigDecimal.ZERO);
        guest.setRol(Rol.CLIENTE);

        when(usuarioRepository.findByEmail("invitado@deustosport.com"))
                .thenReturn(Optional.of(guest));

        ResponseEntity<LoginResponse> resp = loginController.loginGuest();

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertTrue(resp.getBody().isExitoso());
        log.info("[TEST] Invitado autenticado: {}", resp.getBody().getEmail());
    }

    @Test
    void loginGuest_usuarioNuevo_creaYDevuelve200() {
        log.info("[TEST] loginGuest - invitado no existe, se crea → 200");
        Usuario nuevoGuest = new Usuario();
        nuevoGuest.setId(2L);
        nuevoGuest.setNombreCompleto("No Socio");
        nuevoGuest.setEmail("invitado@deustosport.com");
        nuevoGuest.setEsSocio(false);
        nuevoGuest.setBilletera(BigDecimal.ZERO);
        nuevoGuest.setRol(Rol.CLIENTE);

        when(usuarioRepository.findByEmail("invitado@deustosport.com"))
                .thenReturn(Optional.empty());
        when(usuarioRepository.save(any())).thenReturn(nuevoGuest);

        ResponseEntity<LoginResponse> resp = loginController.loginGuest();

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        verify(usuarioRepository).save(any(Usuario.class));
    }

    // ── POST /api/auth/logout ─────────────────────────────────────────────────

    @Test
    void logout_idValido_servicioExitoso_devuelve200() {
        log.info("[TEST] logout - ID válido, servicio exitoso → 200");
        LoginResponse ok = new LoginResponse(1L, null, null, null,
                false, null, "Sesión cerrada", true);
        when(loginService.cerrarSesion(1L)).thenReturn(ok);

        ResponseEntity<LoginResponse> resp = loginController.logout(1L);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertTrue(resp.getBody().isExitoso());
    }

    @Test
    void logout_idCero_devuelve400() {
        log.info("[TEST] logout - ID = 0 → 400");
        ResponseEntity<LoginResponse> resp = loginController.logout(0L);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    void logout_idNegativo_devuelve400() {
        log.info("[TEST] logout - ID negativo → 400");
        ResponseEntity<LoginResponse> resp = loginController.logout(-5L);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    void logout_servicioFalla_devuelve400() {
        log.info("[TEST] logout - servicio falla → 400");
        LoginResponse fail = new LoginResponse(null, null, null, null,
                false, null, "Error al cerrar sesión", false);
        when(loginService.cerrarSesion(5L)).thenReturn(fail);

        ResponseEntity<LoginResponse> resp = loginController.logout(5L);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertFalse(resp.getBody().isExitoso());
    }

    // ── POST /api/auth/solicitar-recuperacion ─────────────────────────────────

    @Test
    void solicitarRecuperacion_emailValido_devuelve200() {
        log.info("[TEST] solicitarRecuperacion - email válido → 200");
        LoginResponse ok = new LoginResponse(null, null, null, null,
                false, null, "Email enviado", true);
        when(loginService.solicitarRecuperacion("juan@test.com")).thenReturn(ok);

        ResponseEntity<LoginResponse> resp = loginController.solicitarRecuperacion("juan@test.com");

        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }

    @Test
    void solicitarRecuperacion_emailVacio_devuelve400() {
        log.info("[TEST] solicitarRecuperacion - email vacío → 400");
        ResponseEntity<LoginResponse> resp = loginController.solicitarRecuperacion("");
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    void solicitarRecuperacion_emailNulo_devuelve400() {
        log.info("[TEST] solicitarRecuperacion - email nulo → 400");
        ResponseEntity<LoginResponse> resp = loginController.solicitarRecuperacion(null);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    // ── POST /api/auth/restablecer-password ───────────────────────────────────

    @Test
    void restablecerPassword_tokenValido_devuelve200() {
        log.info("[TEST] restablecerPassword - token válido → 200");
        LoginResponse ok = new LoginResponse(null, null, null, null,
                false, null, "Contraseña restablecida", true);
        when(loginService.restablecerPassword(any())).thenReturn(ok);

        ResponseEntity<LoginResponse> resp = loginController.restablecerPassword(
                new CambioPasswordRequest("tokenValido123", "nuevaPassword123"));

        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }

    @Test
    void restablecerPassword_sinToken_devuelve400() {
        log.info("[TEST] restablecerPassword - token nulo → 400");
        ResponseEntity<LoginResponse> resp = loginController.restablecerPassword(
                new CambioPasswordRequest(null, "nuevaPassword123"));

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    void restablecerPassword_sinPasswordNueva_devuelve400() {
        log.info("[TEST] restablecerPassword - password nueva nula → 400");
        ResponseEntity<LoginResponse> resp = loginController.restablecerPassword(
                new CambioPasswordRequest("token123", null));

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    void restablecerPassword_tokenInvalido_devuelve401() {
        log.info("[TEST] restablecerPassword - token inválido → 401");
        LoginResponse fail = new LoginResponse(null, null, null, null,
                false, null, "Token inválido o expirado", false);
        when(loginService.restablecerPassword(any())).thenReturn(fail);

        ResponseEntity<LoginResponse> resp = loginController.restablecerPassword(
                new CambioPasswordRequest("tokenMalo", "nuevaPassword123"));

        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
    }

    // ── POST /api/auth/cambiar-password ──────────────────────────────────────

    @Test
    void cambiarPassword_exitoso_devuelve200() {
        log.info("[TEST] cambiarPassword - datos válidos → 200");
        LoginResponse ok = new LoginResponse(null, null, null, null,
                false, null, "Contraseña cambiada", true);
        when(loginService.cambiarPassword(eq(1L), anyString(), anyString())).thenReturn(ok);

        ResponseEntity<LoginResponse> resp = loginController.cambiarPassword(
                1L, new CambioPasswordRequest("actual123", "nueva123"));

        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }

    @Test
    void cambiarPassword_idCero_devuelve400() {
        log.info("[TEST] cambiarPassword - ID = 0 → 400");
        ResponseEntity<LoginResponse> resp = loginController.cambiarPassword(
                0L, new CambioPasswordRequest("actual", "nueva"));
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    void cambiarPassword_sinPasswordActual_devuelve400() {
        log.info("[TEST] cambiarPassword - sin password actual → 400");
        ResponseEntity<LoginResponse> resp = loginController.cambiarPassword(
                1L, new CambioPasswordRequest(null, "nueva123"));
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    void cambiarPassword_servicioFalla_devuelve400() {
        log.info("[TEST] cambiarPassword - servicio falla → 400");
        LoginResponse fail = new LoginResponse(null, null, null, null,
                false, null, "Contraseña actual incorrecta", false);
        when(loginService.cambiarPassword(eq(1L), anyString(), anyString())).thenReturn(fail);

        ResponseEntity<LoginResponse> resp = loginController.cambiarPassword(
                1L, new CambioPasswordRequest("incorrecta", "nueva123"));

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    // ── GET /api/auth/perfil ─────────────────────────────────────────────────

    @Test
    void obtenerPerfil_idValido_devuelve200() {
        log.info("[TEST] obtenerPerfil - ID válido → 200");
        PerfilUsuarioResponse ok = new PerfilUsuarioResponse(1L, "Juan García",
                "juan@test.com", "612345678", "CLIENTE", true, BigDecimal.ZERO, "OK", true);
        when(loginService.obtenerPerfil(1L)).thenReturn(ok);

        ResponseEntity<PerfilUsuarioResponse> resp = loginController.obtenerPerfil(1L);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertTrue(resp.getBody().isExitoso());
    }

    @Test
    void obtenerPerfil_idCero_devuelve400() {
        log.info("[TEST] obtenerPerfil - ID = 0 → 400");
        ResponseEntity<PerfilUsuarioResponse> resp = loginController.obtenerPerfil(0L);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    void obtenerPerfil_noEncontrado_devuelve404() {
        log.info("[TEST] obtenerPerfil - usuario no encontrado → 404");
        PerfilUsuarioResponse notFound = new PerfilUsuarioResponse(null, null, null,
                null, null, false, null, "No encontrado", false);
        when(loginService.obtenerPerfil(99L)).thenReturn(notFound);

        ResponseEntity<PerfilUsuarioResponse> resp = loginController.obtenerPerfil(99L);

        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
    }

    // ── GET /api/auth/verificar-usuario ──────────────────────────────────────

    @Test
    void verificarUsuario_emailExistente_devuelve200() {
        log.info("[TEST] verificarUsuario - email existe → 200");
        Usuario u = new Usuario();
        u.setNombreCompleto("Juan García");
        when(usuarioRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(u));

        ResponseEntity<Map<String, Object>> resp = loginController.verificarUsuario("juan@test.com");

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertTrue((Boolean) resp.getBody().get("existe"));
    }

    @Test
    void verificarUsuario_emailNoExiste_devuelve404() {
        log.info("[TEST] verificarUsuario - email no encontrado → 404");
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        ResponseEntity<Map<String, Object>> resp = loginController.verificarUsuario("noexiste@test.com");

        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        assertFalse((Boolean) resp.getBody().get("existe"));
    }

    @Test
    void verificarUsuario_emailVacio_devuelve400() {
        log.info("[TEST] verificarUsuario - email vacío → 400");
        ResponseEntity<Map<String, Object>> resp = loginController.verificarUsuario("");
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }
}
