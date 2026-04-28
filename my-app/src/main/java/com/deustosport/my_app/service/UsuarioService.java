package com.deustosport.my_app.service;

import com.deustosport.my_app.entity.Usuario;
import com.deustosport.my_app.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    /**
     * Registra un nuevo usuario
     */
    @Transactional
    public Usuario registrarUsuario(String dni, String nombre, String apellidos,
                                    String email, String telefono, String password,
                                    boolean esSocio) {
        if (usuarioRepository.existsByDni(dni)) {
            throw new IllegalArgumentException("Ya existe un usuario con ese DNI");
        }
        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Ya existe un usuario con ese email");
        }

        Usuario usuario = new Usuario();
        usuario.setDni(dni);
        usuario.setNombre(nombre);
        usuario.setApellidos(apellidos);
        usuario.setEmail(email);
        usuario.setTelefono(telefono);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setEsSocio(esSocio);
        usuario.setBilletera(BigDecimal.ZERO);
        usuario.setActivo(true);

        Usuario saved = usuarioRepository.save(usuario);

        // Enviar email de bienvenida
        emailService.enviarEmailBienvenida(saved.getEmail(), saved.getNombre());

        return saved;
    }

    /**
     * Actualiza el perfil del usuario
     */
    @Transactional
    public Usuario actualizarPerfil(Long usuarioId, String nombre, String apellidos,
                                    String telefono, String email) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        usuario.setNombre(nombre);
        usuario.setApellidos(apellidos);
        usuario.setTelefono(telefono);

        // Si cambia el email, verificar que no esté en uso
        if (!usuario.getEmail().equals(email)) {
            if (usuarioRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("El email ya está en uso");
            }
            usuario.setEmail(email);
        }

        return usuarioRepository.save(usuario);
    }

    /**
     * Cambia la contraseña del usuario
     */
    @Transactional
    public void cambiarPassword(Long usuarioId, String oldPassword, String newPassword) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (!passwordEncoder.matches(oldPassword, usuario.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        }

        usuario.setPassword(passwordEncoder.encode(newPassword));
        usuarioRepository.save(usuario);
    }

    /**
     * Recarga la billetera del usuario
     */
    @Transactional
    public Usuario recargarBilletera(Long usuarioId, BigDecimal cantidad) {
        if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser positiva");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        usuario.setBilletera(usuario.getBilletera().add(cantidad));
        return usuarioRepository.save(usuario);
    }

    /**
     * Busca usuarios por DNI (para secretaría)
     */
    public List<Usuario> buscarPorDni(String dni) {
        return usuarioRepository.findByDniContainingIgnoreCase(dni);
    }

    /**
     * Obtiene usuario por ID
     */
    public Optional<Usuario> obtenerPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    /**
     * Desactiva un usuario
     */
    @Transactional
    public void desactivarUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    /**
     * Activa un usuario
     */
    @Transactional
    public void activarUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        usuario.setActivo(true);
        usuarioRepository.save(usuario);
    }

    /**
     * Lista todos los usuarios activos
     */
    public List<Usuario> listarUsuariosActivos() {
        return usuarioRepository.findByActivoTrue();
    }

    /**
     * Verifica si un usuario es socio
     */
    public boolean esSocio(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .map(Usuario::isEsSocio)
                .orElse(false);
    }

    /**
     * Obtiene el saldo de la billetera
     */
    public BigDecimal obtenerSaldoBilletera(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .map(Usuario::getBilletera)
                .orElse(BigDecimal.ZERO);
    }
}