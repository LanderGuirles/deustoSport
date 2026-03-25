package com.deustosport.my_app.service;

import com.deustosport.my_app.dto.CambioPasswordRequest;
import com.deustosport.my_app.dto.LoginRequest;
import com.deustosport.my_app.dto.LoginResponse;
import com.deustosport.my_app.dto.RegistroRequest;
import com.deustosport.my_app.entity.Credencial;
import com.deustosport.my_app.entity.Usuario;
import com.deustosport.my_app.enums.Rol;
import com.deustosport.my_app.repository.CredencialRepository;
import com.deustosport.my_app.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Objects;
import java.util.UUID;

@Service
public class LoginService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CredencialRepository credencialRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public LoginResponse registrarUsuario(RegistroRequest solicitud) {
        String email = solicitud.getEmail().trim().toLowerCase();
        String dni = solicitud.getDni().trim().toUpperCase();

        if (usuarioRepository.existsByEmail(email)) {
            return new LoginResponse(null, null, solicitud.getEmail(), null,
                    "El email ya está registrado", false);
        }

        if (usuarioRepository.existsByDni(dni)) {
            return new LoginResponse(null, null, solicitud.getEmail(), null,
                    "El DNI ya está registrado", false);
        }

        try {
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setNombreCompleto(solicitud.getNombreCompleto().trim());
            nuevoUsuario.setEmail(email);
            nuevoUsuario.setDni(dni);
            nuevoUsuario.setTelefono(solicitud.getTelefono() != null ? solicitud.getTelefono().trim() : null);
            nuevoUsuario.setActivo(true);
            
            // --- SEGURIDAD: FORZAMOS EL ROL A CLIENTE ---
            nuevoUsuario.setRol(Rol.CLIENTE);

            Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);

            Credencial credencial = new Credencial();
            credencial.setUsuario(usuarioGuardado);
            credencial.setPasswordHash(passwordEncoder.encode(solicitud.getPassword()));
            credencial.setActivo(true);
            credencial.setFechaCreacion(LocalDateTime.now());
            credencialRepository.save(credencial);

            // Devolvemos el rol en la respuesta exitosa
                return new LoginResponse(usuarioGuardado.getId(), usuarioGuardado.getNombreCompleto(),
                    usuarioGuardado.getEmail(), toRolString(usuarioGuardado.getRol()), "Usuario registrado exitosamente", true);
        } catch (Exception e) {
            return new LoginResponse(null, null, solicitud.getEmail(), null,
                    "Error al registrar usuario: " + e.getMessage(), false);
        }
    }

    @Transactional
    public LoginResponse iniciarSesion(LoginRequest solicitud) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(solicitud.getEmail());

        if (usuarioOpt.isEmpty()) {
            return new LoginResponse(null, null, solicitud.getEmail(), null,
                    "Email o contraseña incorrectos", false);
        }

        Usuario usuario = usuarioOpt.get();

        if (!usuario.isActivo()) {
            return new LoginResponse(null, null, solicitud.getEmail(), null,
                    "Usuario inactivo", false);
        }

        Optional<Credencial> credencialOpt = credencialRepository.findByUsuarioId(usuario.getId());

        if (credencialOpt.isEmpty() || !credencialOpt.get().isActivo()) {
            return new LoginResponse(null, null, solicitud.getEmail(), null,
                    "Credenciales no válidas", false);
        }

        Credencial credencial = credencialOpt.get();

        if (!passwordEncoder.matches(solicitud.getPassword(), credencial.getPasswordHash())) {
            return new LoginResponse(null, null, solicitud.getEmail(), null,
                    "Email o contraseña incorrectos", false);
        }

        credencial.setUltimoAcceso(LocalDateTime.now());
        credencialRepository.save(credencial);

        // Pasamos el rol en el login exitoso
        return new LoginResponse(usuario.getId(), usuario.getNombreCompleto(),
            usuario.getEmail(), toRolString(usuario.getRol()), "Sesión iniciada exitosamente", true);
    }

    @Transactional
    public LoginResponse cerrarSesion(Long usuarioId) {
        Objects.requireNonNull(usuarioId, "usuarioId no puede ser null");
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);

        if (usuarioOpt.isEmpty()) {
            return new LoginResponse(null, null, null, null,
                    "Usuario no encontrado", false);
        }

        Usuario usuario = usuarioOpt.get();
        // Lógica de credencial (omitida como en tu original)

        return new LoginResponse(usuario.getId(), usuario.getNombreCompleto(),
            usuario.getEmail(), toRolString(usuario.getRol()), "Sesión cerrada exitosamente", true);
    }

    @Transactional
    public LoginResponse solicitarRecuperacion(String email) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

        if (usuarioOpt.isEmpty()) {
            return new LoginResponse(null, null, email, null,
                    "Si el email existe, recibirá instrucciones de recuperación", true);
        }

        Usuario usuario = usuarioOpt.get();
        Optional<Credencial> credencialOpt = credencialRepository.findByUsuarioId(usuario.getId());

        if (credencialOpt.isEmpty()) {
            return new LoginResponse(null, null, email, null,
                    "Error: credenciales no encontradas", false);
        }

        try {
            Credencial credencial = credencialOpt.get();
            String token = UUID.randomUUID().toString();
            credencial.setTokenRecuperacion(token);
            credencial.setFechaExpiracionToken(LocalDateTime.now().plusHours(24));
            credencialRepository.save(credencial);

                return new LoginResponse(usuario.getId(), usuario.getNombreCompleto(),
                    usuario.getEmail(), toRolString(usuario.getRol()), "Instrucciones enviadas al email", true);
        } catch (Exception e) {
            return new LoginResponse(null, null, email, null,
                    "Error al procesar solicitud de recuperación", false);
        }
    }

    @Transactional
    public LoginResponse restablecerPassword(CambioPasswordRequest solicitud) {
        Optional<Credencial> credencialOpt = credencialRepository
                .findByTokenRecuperacion(solicitud.getEmailOToken());

        if (credencialOpt.isEmpty()) {
            return new LoginResponse(null, null, null, null,
                    "Token inválido o expirado", false);
        }

        Credencial credencial = credencialOpt.get();

        if (credencial.getFechaExpiracionToken() == null ||
                LocalDateTime.now().isAfter(credencial.getFechaExpiracionToken())) {
            return new LoginResponse(null, null, null, null,
                    "Token expirado", false);
        }

        try {
            credencial.setPasswordHash(passwordEncoder.encode(solicitud.getPasswordNueva()));
            credencial.setTokenRecuperacion(null);
            credencial.setFechaExpiracionToken(null);
            credencialRepository.save(credencial);

            Usuario usuario = credencial.getUsuario();
                return new LoginResponse(usuario.getId(), usuario.getNombreCompleto(),
                    usuario.getEmail(), toRolString(usuario.getRol()), "Contraseña actualizada exitosamente", true);
        } catch (Exception e) {
            return new LoginResponse(null, null, null, null,
                    "Error al actualizar contraseña: " + e.getMessage(), false);
        }
    }

    @Transactional
    public LoginResponse cambiarPassword(Long usuarioId, String passwordActual, String passwordNueva) {
        Objects.requireNonNull(usuarioId, "usuarioId no puede ser null");
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);

        if (usuarioOpt.isEmpty()) {
            return new LoginResponse(null, null, null, null,
                    "Usuario no encontrado", false);
        }

        Usuario usuario = usuarioOpt.get();
        Optional<Credencial> credencialOpt = credencialRepository.findByUsuarioId(usuarioId);

        if (credencialOpt.isEmpty()) {
            return new LoginResponse(null, null, usuario.getEmail(), null,
                    "Credenciales no encontradas", false);
        }

        Credencial credencial = credencialOpt.get();

        if (!passwordEncoder.matches(passwordActual, credencial.getPasswordHash())) {
            return new LoginResponse(null, null, usuario.getEmail(), null,
                    "Contraseña actual incorrecta", false);
        }

        try {
            credencial.setPasswordHash(passwordEncoder.encode(passwordNueva));
            credencialRepository.save(credencial);

                return new LoginResponse(usuario.getId(), usuario.getNombreCompleto(),
                    usuario.getEmail(), toRolString(usuario.getRol()), "Contraseña actualizada exitosamente", true);
        } catch (Exception e) {
            return new LoginResponse(null, null, usuario.getEmail(), null,
                    "Error al cambiar contraseña: " + e.getMessage(), false);
        }
    }

    private String toRolString(Rol rol) {
        return rol != null ? rol.name() : null;
    }
}