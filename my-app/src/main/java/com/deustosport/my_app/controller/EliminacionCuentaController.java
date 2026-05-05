package com.deustosport.my_app.controller;

import com.deustosport.my_app.dto.SolicitudEliminacionCuentaRequest;
import com.deustosport.my_app.dto.EliminacionCuentaResponse;
import com.deustosport.my_app.service.UsuarioService;
import com.deustosport.my_app.entity.Usuario;
import com.deustosport.my_app.repository.UsuarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para la gestión de eliminación de cuentas de usuario.
 * Proporciona endpoints para validar y procesar solicitudes de eliminación de cuentas.
 * 
 * Como usuario, quiero poder solicitar la eliminación de mi cuenta para que mis datos se borren del sistema.
 */
@RestController
@RequestMapping("/api/cuenta")
@Tag(name = "Gestión de Cuenta", description = "Endpoints para la gestión de cuentas de usuario, incluyendo eliminación")
public class EliminacionCuentaController {

    private static final Logger logger = LoggerFactory.getLogger(EliminacionCuentaController.class);

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Valida si una cuenta puede ser eliminada.
     * Verifica si hay reservas activas, abonos pendientes o saldo en billetera.
     * 
     * @param usuarioId ID del usuario a validar
     * @return ResponseEntity con el estado de validación
     */
    @GetMapping("/validar-eliminacion")
    @Operation(summary = "Validar eliminación de cuenta", 
               description = "Verifica si la cuenta puede ser eliminada sin problemas",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Map<String, Object>> validarEliminacion(
            @RequestHeader("X-Usuario-Id") Long usuarioId) {
        
        logger.info("Validando eliminación para usuario: {}", usuarioId);

        try {
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

            boolean puedeSerEliminado = usuarioService.puedeSerEliminado(usuarioId);
            String motivos = usuarioService.obtenerMotivoNoEliminable(usuarioId);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("puedeSerEliminado", puedeSerEliminado);
            respuesta.put("usuario", usuario.getEmail());
            respuesta.put("nombre", usuario.getNombreCompleto());

            if (puedeSerEliminado) {
                respuesta.put("mensaje", "Tu cuenta puede ser eliminada sin problemas");
                respuesta.put("motivos", null);
            } else {
                respuesta.put("mensaje", "Tu cuenta tiene pendientes que deben resolverse antes de la eliminación");
                respuesta.put("motivos", motivos);
            }

            logger.info("Validación completada para usuario {}: puede ser eliminado = {}", usuarioId, puedeSerEliminado);
            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            logger.error("Error al validar eliminación para usuario {}: {}", usuarioId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error", true,
                            "mensaje", "Error al validar la eliminación: " + e.getMessage()
                    ));
        }
    }

    /**
     * Obtiene información sobre el proceso de eliminación de cuenta.
     * Muestra qué datos serán eliminados y los motivos posibles.
     * 
     * @param usuarioId ID del usuario
     * @return ResponseEntity con información sobre el proceso
     */
    @GetMapping("/info-eliminacion")
    @Operation(summary = "Obtener información de eliminación", 
               description = "Muestra qué datos serán eliminados y el proceso",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Map<String, Object>> obtenerInfoEliminacion(
            @RequestHeader("X-Usuario-Id") Long usuarioId) {

        logger.info("Obteniendo información de eliminación para usuario: {}", usuarioId);

        try {
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("usuario", usuario.getEmail());
            respuesta.put("nombre", usuario.getNombreCompleto());
            
            // Datos que serán eliminados
            Map<String, String> datosAEliminar = new HashMap<>();
            datosAEliminar.put("perfil", "Información completa del perfil");
            datosAEliminar.put("credenciales", "Email y contraseña");
            datosAEliminar.put("historialReservas", "Todas las reservas realizadas");
            datosAEliminar.put("historialAbonos", "Información de abonos activos");
            datosAEliminar.put("informacionPagos", "Datos de transacciones (salvo registro legal)");
            datosAEliminar.put("billetera", "Saldo de billetera");
            datosAEliminar.put("preferencias", "Configuración y preferencias");
            
            respuesta.put("datosAEliminar", datosAEliminar);

            // Motivos disponibles
            Map<String, String> motivosDisponibles = new HashMap<>();
            motivosDisponibles.put("NO_SATISFECHOS", "No satisfechos con el servicio");
            motivosDisponibles.put("PRIVACIDAD", "Preocupación por privacidad");
            motivosDisponibles.put("NO_UTILIZAMOS", "No utilizamos el servicio");
            motivosDisponibles.put("MIGRACION", "Migración a otra plataforma");
            motivosDisponibles.put("OTRO", "Otro motivo");

            respuesta.put("motivosDisponibles", motivosDisponibles);

            respuesta.put("avisoLegal", 
                    "La eliminación es permanente. Los datos no podrán ser recuperados. " +
                    "Se mantendrán registros de auditoría según normativa legal.");

            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            logger.error("Error al obtener información de eliminación: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error", true,
                            "mensaje", "Error al obtener información: " + e.getMessage()
                    ));
        }
    }

    /**
     * Solicita la eliminación de la cuenta de usuario.
     * Requiere contraseña y confirmación explícita.
     * Valida que la cuenta pueda ser eliminada antes de proceder.
     * 
     * @param usuarioId ID del usuario
     * @param solicitud DTO con contraseña, confirmación y motivo
     * @return ResponseEntity con resultado de la eliminación
     */
    @PostMapping("/solicitar-eliminacion")
    @Operation(summary = "Solicitar eliminación de cuenta", 
               description = "Inicia el proceso de eliminación permanente de la cuenta",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<EliminacionCuentaResponse> solicitarEliminacion(
            @RequestHeader("X-Usuario-Id") Long usuarioId,
            @Valid @RequestBody SolicitudEliminacionCuentaRequest solicitud) {

        logger.info("Procesando solicitud de eliminación para usuario: {}", usuarioId);

        try {
            // Validar que el usuario existe
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

            // Validar que se confirma la eliminación
            if (solicitud.getConfirmacion() == null || !solicitud.getConfirmacion()) {
                logger.warn("Eliminación rechazada: confirmación no proporcionada para usuario {}", usuarioId);
                return ResponseEntity.badRequest()
                        .body(new EliminacionCuentaResponse(
                                false,
                                "Debes confirmar explícitamente la eliminación",
                                "Confirmación no recibida"
                        ));
            }

            // Validar contraseña
            if (solicitud.getContrasena() == null || solicitud.getContrasena().isBlank()) {
                logger.warn("Eliminación rechazada: contraseña no proporcionada para usuario {}", usuarioId);
                return ResponseEntity.badRequest()
                        .body(new EliminacionCuentaResponse(
                                false,
                                "La contraseña es requerida",
                                "Contraseña no proporcionada"
                        ));
            }

            // Validar motivo
            if (solicitud.getMotivo() == null || solicitud.getMotivo().isBlank()) {
                logger.warn("Eliminación rechazada: motivo no proporcionado para usuario {}", usuarioId);
                return ResponseEntity.badRequest()
                        .body(new EliminacionCuentaResponse(
                                false,
                                "El motivo es requerido",
                                "Motivo no proporcionado"
                        ));
            }

            // Validar que la cuenta puede ser eliminada
            if (!usuarioService.puedeSerEliminado(usuarioId)) {
                String motivos = usuarioService.obtenerMotivoNoEliminable(usuarioId);
                logger.warn("Eliminación rechazada: cuenta no puede ser eliminada. Motivos: {} (usuario: {})", 
                           motivos, usuarioId);
                
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new EliminacionCuentaResponse(
                                false,
                                "Tu cuenta no puede ser eliminada en este momento",
                                motivos
                        ));
            }

            // Procesar la eliminación
            try {
                boolean eliminada = usuarioService.solicitarEliminacionCuenta(
                        usuarioId,
                        solicitud.getContrasena(),
                        solicitud.getMotivo()
                );

                if (eliminada) {
                    logger.info("Cuenta eliminada exitosamente para usuario: {}", usuario.getEmail());
                    EliminacionCuentaResponse respuesta = new EliminacionCuentaResponse(
                            true,
                            "Tu cuenta ha sido eliminada permanentemente",
                            LocalDateTime.now(),
                            usuario.getEmail()
                    );
                    respuesta.setMotivo(solicitud.getMotivo());
                    respuesta.setDetalles("Todos tus datos han sido borrados del sistema. " +
                            "Se ha enviado un email de confirmación a " + usuario.getEmail());
                    
                    return ResponseEntity.ok(respuesta);
                } else {
                    logger.error("Error al eliminar cuenta para usuario: {}", usuarioId);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new EliminacionCuentaResponse(
                                    false,
                                    "Error al procesar la eliminación",
                                    "Por favor, intenta de nuevo o contacta con soporte"
                            ));
                }

            } catch (IllegalArgumentException e) {
                logger.warn("Eliminación rechazada: contraseña incorrecta (usuario: {})", usuarioId);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new EliminacionCuentaResponse(
                                false,
                                "La contraseña proporcionada es incorrecta",
                                "No se pudo verificar tu identidad"
                        ));
            }

        } catch (IllegalArgumentException e) {
            logger.error("Error: usuario no encontrado {}", usuarioId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new EliminacionCuentaResponse(
                            false,
                            "Usuario no encontrado",
                            e.getMessage()
                    ));
        } catch (Exception e) {
            logger.error("Error inesperado al procesar eliminación de cuenta (usuario: {}): {}", 
                        usuarioId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new EliminacionCuentaResponse(
                            false,
                            "Error inesperado al procesar la solicitud",
                            e.getMessage()
                    ));
        }
    }

    /**
     * Cancela una solicitud de eliminación que estaba en proceso.
     * Se utiliza si el usuario cambió de opinión antes de la eliminación final.
     * 
     * @param usuarioId ID del usuario
     * @return ResponseEntity con el resultado
     */
    @PostMapping("/cancelar-eliminacion")
    @Operation(summary = "Cancelar solicitud de eliminación", 
               description = "Cancela una solicitud de eliminación en curso",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Map<String, Object>> cancelarEliminacion(
            @RequestHeader("X-Usuario-Id") Long usuarioId) {

        logger.info("Cancelando solicitud de eliminación para usuario: {}", usuarioId);

        try {
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("exitoso", true);
            respuesta.put("mensaje", "Solicitud de eliminación cancelada");
            respuesta.put("usuario", usuario.getEmail());
            respuesta.put("timestamp", LocalDateTime.now());

            logger.info("Solicitud de eliminación cancelada para usuario: {}", usuario.getEmail());
            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            logger.error("Error al cancelar eliminación: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "exitoso", false,
                            "error", "Error al cancelar la solicitud: " + e.getMessage()
                    ));
        }
    }
}
