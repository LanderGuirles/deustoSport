package com.deustosport.my_app.service;

import com.deustosport.my_app.entity.Usuario;
import com.deustosport.my_app.entity.Credencial;
import com.deustosport.my_app.enums.Rol;
import com.deustosport.my_app.repository.AyuntamientoRepository;
import com.deustosport.my_app.repository.CredencialRepository;
import com.deustosport.my_app.repository.PolideportivoRepository;
import com.deustosport.my_app.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final CredencialRepository credencialRepository;
    private final PolideportivoRepository polideportivoRepository;
    private final AyuntamientoRepository ayuntamientoRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          CredencialRepository credencialRepository,
                          PolideportivoRepository polideportivoRepository,
                          AyuntamientoRepository ayuntamientoRepository,
                          PasswordEncoder passwordEncoder,
                          EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.credencialRepository = credencialRepository;
        this.polideportivoRepository = polideportivoRepository;
        this.ayuntamientoRepository = ayuntamientoRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    /**
     * Registra un nuevo usuario
     */
    @Transactional
    public Usuario registrarUsuario(String dni, String nombre, String apellidos,
                                    String email, String telefono, String password,
                                    boolean esSocio, Rol rol, Long polideportivoId, Long ayuntamientoId) {
        if (usuarioRepository.existsByDni(dni)) {
            throw new IllegalArgumentException("Ya existe un usuario con ese DNI");
        }
        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Ya existe un usuario con ese email");
        }

        Usuario usuario = new Usuario();
        usuario.setDni(dni);
        usuario.setNombreCompleto(construirNombreCompleto(nombre, apellidos));
        usuario.setEmail(email);
        usuario.setTelefono(telefono);
        usuario.setEsSocio(esSocio);
        usuario.setBilletera(BigDecimal.ZERO);
        usuario.setActivo(true);
        usuario.setRol(rol != null ? rol : Rol.CLIENTE);

        // Validaciones y asociaciones según rol
        if (usuario.getRol() == Rol.SECRETARIA || usuario.getRol() == Rol.COORDINADOR || usuario.getRol() == Rol.MANTENIMIENTO) {
            if (polideportivoId == null) {
                throw new IllegalArgumentException("Los usuarios de tipo " + usuario.getRol() + " deben estar asociados a un polideportivo");
            }
            usuario.setPolideportivo(polideportivoRepository.findById(polideportivoId)
                    .orElseThrow(() -> new IllegalArgumentException("Polideportivo no encontrado")));
        } else if (usuario.getRol() == Rol.AYUNTAMIENTO) {
            if (ayuntamientoId == null) {
                throw new IllegalArgumentException("Los usuarios de tipo AYUNTAMIENTO deben estar asociados a un ayuntamiento");
            }
            usuario.setAyuntamiento(ayuntamientoRepository.findById(ayuntamientoId)
                    .orElseThrow(() -> new IllegalArgumentException("Ayuntamiento no encontrado")));
        }

        Usuario saved = usuarioRepository.save(usuario);

        Credencial credencial = new Credencial();
        credencial.setUsuario(saved);
        credencial.setPasswordHash(passwordEncoder.encode(password));
        credencial.setActivo(true);
        credencial.setFechaCreacion(LocalDateTime.now());
        credencialRepository.save(credencial);

        // Enviar email de bienvenida
        emailService.enviarEmailBienvenida(saved.getEmail(), saved.getNombreCompleto());

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

        usuario.setNombreCompleto(construirNombreCompleto(nombre, apellidos));
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
        Credencial credencial = credencialRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (!passwordEncoder.matches(oldPassword, credencial.getPasswordHash())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        }

        credencial.setPasswordHash(passwordEncoder.encode(newPassword));
        credencialRepository.save(credencial);
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

    public List<Usuario> obtenerPorPolideportivoYRoles(Long polideportivoId, List<Rol> roles) {
        return usuarioRepository.findByPolideportivoAndRoles(polideportivoId, roles);
    }

    @Transactional
    public Usuario actualizarUsuarioPolideportivo(Long usuarioId, String nombre, String apellidos,
                                                  String telefono, String email, Rol rol) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        usuario.setNombreCompleto(construirNombreCompleto(nombre, apellidos));
        usuario.setTelefono(telefono);

        if (email != null && !usuario.getEmail().equals(email)) {
            if (usuarioRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("El email ya está en uso");
            }
            usuario.setEmail(email);
        }

        if (rol != null) {
            usuario.setRol(rol);
        }

        return usuarioRepository.save(usuario);
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
     * Elimina un usuario definitivamente
     */
    @Transactional
    public void eliminarUsuarioDefinitivamente(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        credencialRepository.findByUsuarioId(usuarioId).ifPresent(credencialRepository::delete);
        usuarioRepository.delete(usuario);
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

    /**
     * Solicita la eliminación de la cuenta del usuario.
     * Verifica la contraseña y realiza la eliminación del usuario y sus datos asociados.
     * 
     * @param usuarioId ID del usuario a eliminar
     * @param contrasena Contraseña del usuario para confirmación
     * @param motivo Motivo de la eliminación
     * @return true si la eliminación fue exitosa
     * @throws IllegalArgumentException si la contraseña es incorrecta o el usuario no existe
     */
    @Transactional
    public boolean solicitarEliminacionCuenta(Long usuarioId, String contrasena, String motivo) {
        // Verificar que el usuario existe
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Verificar contraseña
        Credencial credencial = credencialRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Credencial del usuario no encontrada"));

        if (!passwordEncoder.matches(contrasena, credencial.getPasswordHash())) {
            throw new IllegalArgumentException("La contraseña es incorrecta. No se puede eliminar la cuenta sin verificación correcta.");
        }

        // Registrar la solicitud de eliminación en auditoría
        registrarEliminacionEnAuditoria(usuario, motivo);

        // Eliminar las credenciales del usuario
        credencialRepository.delete(credencial);

        // Eliminar el usuario (cascada debe eliminar datos asociados)
        usuarioRepository.delete(usuario);

        // Enviar email de confirmación de eliminación
        emailService.enviarEmailEliminacionCuenta(usuario.getEmail(), usuario.getNombreCompleto());

        return true;
    }

    /**
     * Verifica si un usuario puede ser eliminado (validaciones de negocio).
     * Comprueba si hay reservas activas u otros datos críticos.
     * 
     * @param usuarioId ID del usuario a validar
     * @return true si puede ser eliminado
     */
    @Transactional(readOnly = true)
    public boolean puedeSerEliminado(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Validaciones de negocio:
        // 1. No puede tener saldo pendiente en billetera (excepto si es 0 o negativo)
        if (usuario.getBilletera().compareTo(BigDecimal.ZERO) > 0) {
            return false;
        }

        // 2. No puede tener abonos activos
        if (usuario.getAbonos() != null && !usuario.getAbonos().isEmpty()) {
            return false;
        }

        return true;
    }

    /**
     * Obtiene información sobre por qué un usuario no puede ser eliminado.
     * 
     * @param usuarioId ID del usuario a validar
     * @return String con las razones por las que no puede eliminarse
     */
    @Transactional(readOnly = true)
    public String obtenerMotivoNoEliminable(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        StringBuilder motivos = new StringBuilder();

        if (usuario.getBilletera().compareTo(BigDecimal.ZERO) > 0) {
            motivos.append("- Tiene un saldo pendiente en la billetera (")
                    .append(usuario.getBilletera()).append("€). ");
        }

        if (usuario.getAbonos() != null && !usuario.getAbonos().isEmpty()) {
            motivos.append("- Tiene abonos activos. ");
        }

        return motivos.toString().isEmpty() ? 
                "El usuario puede ser eliminado" : 
                motivos.toString();
    }

    /**
     * Registra la eliminación de cuenta en la tabla de auditoría.
     * Esto es importante para mantener un registro de eliminaciones.
     * 
     * @param usuario Usuario eliminado
     * @param motivo Motivo de la eliminación
     */
    private void registrarEliminacionEnAuditoria(Usuario usuario, String motivo) {
        // Nota: Esta operación se debe realizar a través de AuditoriaService
        // Por ahora dejamos como placeholder para que se implemente con AuditoriaService
        System.out.println("AUDITORÍA: Eliminación de cuenta de usuario " + usuario.getEmail() 
                + " - Motivo: " + motivo + " - Fecha: " + LocalDateTime.now());
    }

    private String construirNombreCompleto(String nombre, String apellidos) {
        String nombreLimpio = nombre != null ? nombre.trim() : "";
        String apellidosLimpios = apellidos != null ? apellidos.trim() : "";
        if (nombreLimpio.isEmpty()) {
            return apellidosLimpios;
        }
        if (apellidosLimpios.isEmpty()) {
            return nombreLimpio;
        }
        return nombreLimpio + " " + apellidosLimpios;
    }
}