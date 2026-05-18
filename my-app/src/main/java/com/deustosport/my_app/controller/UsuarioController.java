package com.deustosport.my_app.controller;

import com.deustosport.my_app.dto.*;
import com.deustosport.my_app.entity.Usuario;
import com.deustosport.my_app.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Gestión de usuarios y perfiles")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * Registro de nuevo usuario (solo accesible por AYUNTAMIENTO)
     */
    @PostMapping("/registro")
    @Operation(summary = "Registrar nuevo usuario")
    public ResponseEntity<?> registrarUsuario(@Valid @RequestBody RegistroUsuarioRequest request) {
        try {
            com.deustosport.my_app.enums.Rol rolEnum = null;
            if (request.getRol() != null) {
                try {
                    rolEnum = com.deustosport.my_app.enums.Rol.valueOf(request.getRol().toUpperCase());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Rol no válido: " + request.getRol()));
                }
            }

            Usuario usuario = usuarioService.registrarUsuario(
                    request.getDni(),
                    request.getNombre(),
                    request.getApellidos(),
                    request.getEmail(),
                    request.getTelefono(),
                    request.getPassword(),
                    request.isEsSocio(),
                    rolEnum,
                    request.getPolideportivoId(),
                    request.getAyuntamientoId()
            );

            RegistroUsuarioResponse response = new RegistroUsuarioResponse();
            response.setId(usuario.getId());
            response.setNombre(extraerNombre(usuario.getNombreCompleto()));
            response.setApellidos(extraerApellidos(usuario.getNombreCompleto()));
            response.setEmail(usuario.getEmail());
            response.setEsSocio(usuario.isEsSocio());
            response.setMensaje("Usuario registrado exitosamente");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Obtener perfil del usuario actual
     */
    @GetMapping("/perfil")
    @Operation(summary = "Obtener perfil del usuario autenticado")
    public ResponseEntity<UsuarioPerfilResponse> obtenerPerfil(Authentication auth) {
        // Asumiendo que el username es el ID del usuario
        Long usuarioId = Long.parseLong(auth.getName());

        Usuario usuario = usuarioService.obtenerPorId(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        UsuarioPerfilResponse response = new UsuarioPerfilResponse();
        response.setId(usuario.getId());
        response.setDni(usuario.getDni());
        response.setNombre(extraerNombre(usuario.getNombreCompleto()));
        response.setApellidos(extraerApellidos(usuario.getNombreCompleto()));
        response.setEmail(usuario.getEmail());
        response.setTelefono(usuario.getTelefono());
        response.setEsSocio(usuario.isEsSocio());
        response.setBilletera(usuario.getBilletera());
        response.setActivo(usuario.isActivo());

        return ResponseEntity.ok(response);
    }

    /**
     * Actualizar perfil del usuario
     */
    @PutMapping("/perfil")
    @Operation(summary = "Actualizar perfil del usuario")
    public ResponseEntity<?> actualizarPerfil(Authentication auth,
                                              @Valid @RequestBody ActualizarPerfilRequest request) {
        Long usuarioId = Long.parseLong(auth.getName());
        Usuario usuarioActual = usuarioService.obtenerPorId(usuarioId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        try {
            usuarioService.actualizarPerfil(
                    usuarioId,
                    request.getNombreCompleto(),
                    "",
                    request.getTelefono(),
                usuarioActual.getEmail()
            );

            return ResponseEntity.ok(Map.of("mensaje", "Perfil actualizado exitosamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Cambiar contraseña
     */
    @PostMapping("/cambiar-password")
    @Operation(summary = "Cambiar contraseña del usuario")
    public ResponseEntity<?> cambiarPassword(Authentication auth,
                                             @Valid @RequestBody CambiarPasswordRequest request) {
        Long usuarioId = Long.parseLong(auth.getName());

        try {
            usuarioService.cambiarPassword(
                    usuarioId,
                    request.getOldPassword(),
                    request.getNewPassword()
            );
            return ResponseEntity.ok(Map.of("mensaje", "Contraseña cambiada exitosamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Recargar billetera
     */
    @PostMapping("/recargar-billetera")
    @Operation(summary = "Recargar saldo de la billetera")
    public ResponseEntity<?> recargarBilletera(Authentication auth,
                                               @Valid @RequestBody RecargarBilleteraRequest request) {
        Long usuarioId = Long.parseLong(auth.getName());

        try {
            Usuario usuario = usuarioService.recargarBilletera(usuarioId, request.getCantidad());
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Billetera recargada exitosamente",
                    "nuevoSaldo", usuario.getBilletera()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Obtener saldo de billetera
     */
    @GetMapping("/billetera/saldo")
    @Operation(summary = "Obtener saldo actual de la billetera")
    public ResponseEntity<Map<String, BigDecimal>> obtenerSaldoBilletera(Authentication auth) {
        Long usuarioId = Long.parseLong(auth.getName());
        BigDecimal saldo = usuarioService.obtenerSaldoBilletera(usuarioId);
        return ResponseEntity.ok(Map.of("saldo", saldo));
    }

    /**
     * Buscar usuarios por DNI (solo para secretaría)
     */
    @GetMapping("/buscar")
    @PreAuthorize("hasRole('SECRETARIA')")
    @Operation(summary = "Buscar usuarios por DNI")
    public ResponseEntity<List<UsuarioBusquedaResponse>> buscarUsuarios(
            @RequestParam("dni") String dni) {
        List<Usuario> usuarios = usuarioService.buscarPorDni(dni);

        List<UsuarioBusquedaResponse> response = usuarios.stream()
                .map(u -> {
                    UsuarioBusquedaResponse r = new UsuarioBusquedaResponse();
                    r.setId(u.getId());
                    r.setDni(u.getDni());
                r.setNombre(extraerNombre(u.getNombreCompleto()));
                r.setApellidos(extraerApellidos(u.getNombreCompleto()));
                    r.setEmail(u.getEmail());
                    r.setTelefono(u.getTelefono());
                    r.setEsSocio(u.isEsSocio());
                    r.setActivo(u.isActivo());
                    return r;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Listar usuarios activos (solo admin)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos los usuarios activos")
    public ResponseEntity<List<UsuarioListadoResponse>> listarUsuariosActivos() {
        List<Usuario> usuarios = usuarioService.listarUsuariosActivos();

        List<UsuarioListadoResponse> response = usuarios.stream()
                .map(u -> {
                    UsuarioListadoResponse r = new UsuarioListadoResponse();
                    r.setId(u.getId());
                    r.setDni(u.getDni());
                    r.setNombre(extraerNombre(u.getNombreCompleto()));
                    r.setApellidos(extraerApellidos(u.getNombreCompleto()));
                    r.setEmail(u.getEmail());
                    r.setEsSocio(u.isEsSocio());
                    r.setBilletera(u.getBilletera());
                    return r;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Desactivar usuario (admin y ayuntamiento)
     */
    @PostMapping("/{usuarioId}/desactivar")
    @Operation(summary = "Desactivar usuario")
    public ResponseEntity<?> desactivarUsuario(@PathVariable Long usuarioId) {
        try {
            usuarioService.desactivarUsuario(usuarioId);
            return ResponseEntity.ok(Map.of("mensaje", "Usuario desactivado exitosamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/polideportivo/{polideportivoId}")
    @Operation(summary = "Listar usuarios por polideportivo (roles MANTENIMIENTO, SECRETARIA, COORDINADOR)")
    public ResponseEntity<List<UsuarioListadoResponse>> listarUsuariosPorPolideportivo(@PathVariable Long polideportivoId) {
        List<com.deustosport.my_app.enums.Rol> roles = java.util.Arrays.asList(
                com.deustosport.my_app.enums.Rol.MANTENIMIENTO,
                com.deustosport.my_app.enums.Rol.SECRETARIA,
                com.deustosport.my_app.enums.Rol.COORDINADOR
        );
        List<Usuario> usuarios = usuarioService.obtenerPorPolideportivoYRoles(polideportivoId, roles);
        List<UsuarioListadoResponse> response = usuarios.stream()
                .map(u -> {
                    UsuarioListadoResponse r = new UsuarioListadoResponse();
                    r.setId(u.getId());
                    r.setDni(u.getDni());
                    r.setNombre(extraerNombre(u.getNombreCompleto()));
                    r.setApellidos(extraerApellidos(u.getNombreCompleto()));
                    r.setEmail(u.getEmail());
                    r.setRol(u.getRol() != null ? u.getRol().name() : null);
                    return r;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/polideportivo/{usuarioId}")
    @Operation(summary = "Actualizar usuario de un polideportivo")
    public ResponseEntity<?> actualizarUsuarioPolideportivo(
            @PathVariable Long usuarioId,
            @Valid @RequestBody ActualizarUsuarioPoliRequest request) {
        try {
            com.deustosport.my_app.enums.Rol rolEnum = null;
            if (request.getRol() != null) {
                rolEnum = com.deustosport.my_app.enums.Rol.valueOf(request.getRol().toUpperCase());
            }

            usuarioService.actualizarUsuarioPolideportivo(
                    usuarioId,
                    request.getNombre(),
                    request.getApellidos(),
                    request.getTelefono(),
                    request.getEmail(),
                    rolEnum
            );
            return ResponseEntity.ok(Map.of("mensaje", "Usuario actualizado exitosamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/polideportivo/{usuarioId}")
    @Operation(summary = "Eliminar definitivamente un usuario de polideportivo")
    public ResponseEntity<?> eliminarUsuarioPolideportivo(@PathVariable Long usuarioId) {
        try {
            usuarioService.eliminarUsuarioDefinitivamente(usuarioId);
            return ResponseEntity.ok(Map.of("mensaje", "Usuario eliminado exitosamente de la base de datos"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "No se puede eliminar el usuario porque tiene registros asociados (incidencias, reservas, notificaciones, etc.). Debes eliminar esos registros primero o simplemente desactivar la cuenta."));
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error interno al eliminar el usuario: " + e.getMessage()));
        }
    }

    /**
     * Activar usuario (solo admin)
     */
    @PostMapping("/{usuarioId}/activar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activar usuario")
    public ResponseEntity<?> activarUsuario(@PathVariable Long usuarioId) {
        try {
            usuarioService.activarUsuario(usuarioId);
            return ResponseEntity.ok(Map.of("mensaje", "Usuario activado exitosamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private String extraerNombre(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.isBlank()) {
            return "";
        }
        String[] partes = nombreCompleto.trim().split("\\s+", 2);
        return partes[0];
    }

    private String extraerApellidos(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.isBlank()) {
            return "";
        }
        String[] partes = nombreCompleto.trim().split("\\s+", 2);
        return partes.length > 1 ? partes[1] : "";
    }
}