package com.deustosport.my_app.service;

import com.deustosport.my_app.dto.CambioPasswordRequest;
import com.deustosport.my_app.dto.LoginRequest;
import com.deustosport.my_app.dto.LoginResponse;
import com.deustosport.my_app.dto.RegistroRequest;
import com.deustosport.my_app.entity.Credencial;
import com.deustosport.my_app.entity.Usuario;
import com.deustosport.my_app.repository.CredencialRepository;
import com.deustosport.my_app.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class LoginService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CredencialRepository credencialRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Registra un nuevo usuario
     */
    @Transactional
    public LoginResponse registrarUsuario(RegistroRequest solicitud) {
        // Verificar si el email ya existe
        String email = solicitud.getEmail().trim().toLowerCase();
        String dni = solicitud.getDni().trim().toUpperCase();

        if (usuarioRepository.existsByEmail(email)) {
            return new LoginResponse(null, null, solicitud.getEmail(),
                    "El email ya está registrado", false);
        }

        if (usuarioRepository.existsByDni(dni)) {
            return new LoginResponse(null, null, solicitud.getEmail(),
                    "El DNI ya está registrado", false);
        }

        try {
            // Crear nuevo usuario
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setNombreCompleto(solicitud.getNombreCompleto().trim());
            nuevoUsuario.setEmail(email);
            nuevoUsuario.setDni(dni);
            nuevoUsuario.setTelefono(solicitud.getTelefono() != null ? solicitud.getTelefono().trim() : null);
            nuevoUsuario.setActivo(true);

            Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);

            // Crear credenciales
            Credencial credencial = new Credencial();
            credencial.setUsuario(usuarioGuardado);
            credencial.setPasswordHash(passwordEncoder.encode(solicitud.getPassword()));
            credencial.setActivo(true);
            credencial.setFechaCreacion(LocalDateTime.now());
            credencialRepository.save(credencial);

            return new LoginResponse(usuarioGuardado.getId(), usuarioGuardado.getNombreCompleto(),
                    usuarioGuardado.getEmail(), "Usuario registrado exitosamente", true);
        } catch (Exception e) {
            return new LoginResponse(null, null, solicitud.getEmail(),
                    "Error al registrar usuario: " + e.getMessage(), false);
        }
    }

    /**
     * Inicia sesión con email y contraseña
     */
    @Transactional
    public LoginResponse iniciarSesion(LoginRequest solicitud) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(solicitud.getEmail());

        if (usuarioOpt.isEmpty()) {
            return new LoginResponse(null, null, solicitud.getEmail(),
                    "Email o contraseña incorrectos", false);
        }

        Usuario usuario = usuarioOpt.get();

        if (!usuario.isActivo()) {
            return new LoginResponse(null, null, solicitud.getEmail(),
                    "Usuario inactivo", false);
        }

        Optional<Credencial> credencialOpt = credencialRepository.findByUsuarioId(usuario.getId());

        if (credencialOpt.isEmpty() || !credencialOpt.get().isActivo()) {
            return new LoginResponse(null, null, solicitud.getEmail(),
                    "Credenciales no válidas", false);
        }

        Credencial credencial = credencialOpt.get();

        // Verificar contraseña
        if (!passwordEncoder.matches(solicitud.getPassword(), credencial.getPasswordHash())) {
            return new LoginResponse(null, null, solicitud.getEmail(),
                    "Email o contraseña incorrectos", false);
        }

        // Actualizar último acceso
        credencial.setUltimoAcceso(LocalDateTime.now());
        credencialRepository.save(credencial);

        return new LoginResponse(usuario.getId(), usuario.getNombreCompleto(),
                usuario.getEmail(), "Sesión iniciada exitosamente", true);
    }

    /**
     * Cierra la sesión del usuario (registra tiempo de cierre)
     */
    @Transactional
    public LoginResponse cerrarSesion(Long usuarioId) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);

        if (usuarioOpt.isEmpty()) {
            return new LoginResponse(null, null, null,
                    "Usuario no encontrado", false);
        }

        Usuario usuario = usuarioOpt.get();

        Optional<Credencial> credencialOpt = credencialRepository.findByUsuarioId(usuarioId);
        if (credencialOpt.isPresent()) {
            // Aquí podrías guardar el tiempo de cierre si tienes un campo para ello
            // credencialOpt.get().setUltimoCierre(LocalDateTime.now());
            // credencialRepository.save(credencialOpt.get());
        }

        return new LoginResponse(usuario.getId(), usuario.getNombreCompleto(),
                usuario.getEmail(), "Sesión cerrada exitosamente", true);
    }

    /**
     * Solicita recuperación de contraseña generando un token
     */
    @Transactional
    public LoginResponse solicitarRecuperacion(String email) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

        if (usuarioOpt.isEmpty()) {
            // Por seguridad, no revelamos si el email existe
            return new LoginResponse(null, null, email,
                    "Si el email existe, recibirá instrucciones de recuperación", true);
        }

        Usuario usuario = usuarioOpt.get();
        Optional<Credencial> credencialOpt = credencialRepository.findByUsuarioId(usuario.getId());

        if (credencialOpt.isEmpty()) {
            return new LoginResponse(null, null, email,
                    "Error: credenciales no encontradas", false);
        }

        try {
            Credencial credencial = credencialOpt.get();
            // Generar token único válido por 24 horas
            String token = UUID.randomUUID().toString();
            credencial.setTokenRecuperacion(token);
            credencial.setFechaExpiracionToken(LocalDateTime.now().plusHours(24));
            credencialRepository.save(credencial);

            // En producción, aquí enviarías un email con el token
            // mailService.enviarTokenRecuperacion(email, token);

            return new LoginResponse(usuario.getId(), usuario.getNombreCompleto(),
                    usuario.getEmail(), "Instrucciones enviadas al email", true);
        } catch (Exception e) {
            return new LoginResponse(null, null, email,
                    "Error al procesar solicitud de recuperación", false);
        }
    }

    /**
     * Restablece la contraseña usando el token de recuperación
     */
    @Transactional
    public LoginResponse restablecerPassword(CambioPasswordRequest solicitud) {
        Optional<Credencial> credencialOpt = credencialRepository
                .findByTokenRecuperacion(solicitud.getEmailOToken());

        if (credencialOpt.isEmpty()) {
            return new LoginResponse(null, null, null,
                    "Token inválido o expirado", false);
        }

        Credencial credencial = credencialOpt.get();

        // Verificar que el token no haya expirado
        if (credencial.getFechaExpiracionToken() == null ||
                LocalDateTime.now().isAfter(credencial.getFechaExpiracionToken())) {
            return new LoginResponse(null, null, null,
                    "Token expirado", false);
        }

        try {
            // Actualizar contraseña
            credencial.setPasswordHash(passwordEncoder.encode(solicitud.getPasswordNueva()));
            credencial.setTokenRecuperacion(null);
            credencial.setFechaExpiracionToken(null);
            credencialRepository.save(credencial);

            Usuario usuario = credencial.getUsuario();
            return new LoginResponse(usuario.getId(), usuario.getNombreCompleto(),
                    usuario.getEmail(), "Contraseña actualizada exitosamente", true);
        } catch (Exception e) {
            return new LoginResponse(null, null, null,
                    "Error al actualizar contraseña: " + e.getMessage(), false);
        }
    }

    /**
     * Cambia la contraseña de un usuario autenticado
     */
    @Transactional
    public LoginResponse cambiarPassword(Long usuarioId, String passwordActual, String passwordNueva) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);

        if (usuarioOpt.isEmpty()) {
            return new LoginResponse(null, null, null,
                    "Usuario no encontrado", false);
        }

        Usuario usuario = usuarioOpt.get();
        Optional<Credencial> credencialOpt = credencialRepository.findByUsuarioId(usuarioId);

        if (credencialOpt.isEmpty()) {
            return new LoginResponse(null, null, usuario.getEmail(),
                    "Credenciales no encontradas", false);
        }

        Credencial credencial = credencialOpt.get();

        // Verificar contraseña actual
        if (!passwordEncoder.matches(passwordActual, credencial.getPasswordHash())) {
            return new LoginResponse(null, null, usuario.getEmail(),
                    "Contraseña actual incorrecta", false);
        }

        try {
            credencial.setPasswordHash(passwordEncoder.encode(passwordNueva));
            credencialRepository.save(credencial);

            return new LoginResponse(usuario.getId(), usuario.getNombreCompleto(),
                    usuario.getEmail(), "Contraseña actualizada exitosamente", true);
        } catch (Exception e) {
            return new LoginResponse(null, null, usuario.getEmail(),
                    "Error al cambiar contraseña: " + e.getMessage(), false);
        }
    }
}
