package com.deustosport.my_app.controller;

import com.deustosport.my_app.dto.AvisoDelDiaRequest;
import com.deustosport.my_app.dto.AvisoDelDiaResponse;
import com.deustosport.my_app.service.AvisoDelDiaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para la gestión de avisos del día.
 *
 * Expone los endpoints necesarios para que el personal de secretaría
 * pueda publicar, editar, activar/desactivar y eliminar avisos del día,
 * y para que la pantalla de inicio de los usuarios obtenga el aviso
 * activo que debe mostrar en el banner informativo.
 *
 * Base path: /api/avisos
 *
 * Endpoints disponibles:
 * ┌─────────────────────────────────────────────────────────────────────┐
 * │ POST   /api/avisos                  → Publicar nuevo aviso          │
 * │ GET    /api/avisos                  → Listar todos los avisos       │
 * │ GET    /api/avisos/activos          → Listar avisos activos         │
 * │ GET    /api/avisos/activos/principal→ Aviso principal para home     │
 * │ GET    /api/avisos/{id}             → Detalle de un aviso           │
 * │ PUT    /api/avisos/{id}             → Actualizar aviso              │
 * │ PATCH  /api/avisos/{id}/activar     → Activar aviso                │
 * │ PATCH  /api/avisos/{id}/desactivar  → Desactivar aviso             │
 * │ DELETE /api/avisos/{id}             → Eliminar aviso               │
 * │ POST   /api/avisos/limpiar-expirados→ Desactivar caducados         │
 * │ GET    /api/avisos/stats            → Contador de avisos activos    │
 * └─────────────────────────────────────────────────────────────────────┘
 */
@RestController
@RequestMapping("/api/avisos")
@Tag(name = "Avisos del día", description = "Gestión de avisos informativos publicados por secretaría")
public class AvisoDelDiaController {

    private final AvisoDelDiaService avisoService;

    public AvisoDelDiaController(AvisoDelDiaService avisoService) {
        this.avisoService = avisoService;
    }

    // ─────────────────────────────────────────────────────────────
    // Endpoints de escritura (secretaría)
    // ─────────────────────────────────────────────────────────────

    /**
     * Publica un nuevo aviso del día.
     *
     * El aviso se crea activo por defecto y aparece de inmediato en la
     * pantalla de inicio de todos los usuarios del sistema.
     *
     * Requiere: titulo (no vacío), mensaje (no vacío), tipo (INFO|AVISO|URGENTE).
     * Opcionales: fechaExpiracion, prioridad (0-10), creadoPorId, creadoPorNombre.
     *
     * @param request datos del nuevo aviso.
     * @return 201 Created con el aviso creado, o 400 Bad Request con
     *         el campo "error" si los datos de entrada son inválidos.
     */
    @PostMapping
    @Operation(summary = "Publicar un nuevo aviso del día",
               description = "Crea y activa un aviso informativo visible en home.html para todos los usuarios")
    public ResponseEntity<?> publicarAviso(@RequestBody AvisoDelDiaRequest request) {
        try {
            AvisoDelDiaResponse response = avisoService.publicarAviso(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error inesperado al publicar el aviso."));
        }
    }

    /**
     * Actualiza los campos editables de un aviso existente.
     *
     * Registra automáticamente la fecha de edición y el nombre del editor
     * en los campos de auditoría del aviso. El estado activo/inactivo
     * no se modifica mediante este endpoint; usar PATCH /activar o /desactivar.
     *
     * @param id      ID del aviso a actualizar.
     * @param request DTO con los nuevos valores.
     * @return 200 OK con el aviso actualizado, 400 si datos inválidos,
     *         404 si el aviso no existe.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un aviso del día existente",
               description = "Modifica el título, mensaje, tipo, prioridad y fecha de expiración del aviso")
    public ResponseEntity<?> actualizarAviso(
            @Parameter(description = "ID del aviso a actualizar") @PathVariable Long id,
            @RequestBody AvisoDelDiaRequest request) {
        try {
            AvisoDelDiaResponse response = avisoService.actualizarAviso(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error inesperado al actualizar el aviso."));
        }
    }

    /**
     * Activa un aviso para que aparezca en la pantalla de inicio de los usuarios.
     *
     * Si el aviso ya estaba activo, la operación no produce cambios adicionales.
     * Si la fecha de expiración del aviso ha pasado, el aviso quedará activo
     * pero no será devuelto por el endpoint /activos/principal hasta que se
     * actualice la fecha de expiración.
     *
     * @param id ID del aviso a activar.
     * @return 200 OK con el aviso activado, 400 si el ID no existe.
     */
    @PatchMapping("/{id}/activar")
    @Operation(summary = "Activar un aviso del día",
               description = "Marca el aviso como activo para que sea visible en la pantalla de inicio")
    public ResponseEntity<?> activarAviso(
            @Parameter(description = "ID del aviso a activar") @PathVariable Long id) {
        try {
            AvisoDelDiaResponse response = avisoService.activarAviso(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error inesperado al activar el aviso."));
        }
    }

    /**
     * Desactiva un aviso para que deje de mostrarse en la pantalla de inicio.
     *
     * El aviso permanece en base de datos y puede reactivarse en cualquier
     * momento mediante el endpoint PATCH /{id}/activar.
     * Esta operación es la forma recomendada de "archivar" un aviso sin
     * perder su historial.
     *
     * @param id ID del aviso a desactivar.
     * @return 200 OK con el aviso desactivado, 400 si el ID no existe.
     */
    @PatchMapping("/{id}/desactivar")
    @Operation(summary = "Desactivar un aviso del día",
               description = "Oculta el aviso de la pantalla de inicio sin eliminarlo de la base de datos")
    public ResponseEntity<?> desactivarAviso(
            @Parameter(description = "ID del aviso a desactivar") @PathVariable Long id) {
        try {
            AvisoDelDiaResponse response = avisoService.desactivarAviso(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error inesperado al desactivar el aviso."));
        }
    }

    /**
     * Elimina permanentemente un aviso de la base de datos.
     *
     * Esta operación es irreversible. Para ocultar un aviso sin eliminarlo,
     * usar PATCH /{id}/desactivar en su lugar.
     *
     * @param id ID del aviso a eliminar.
     * @return 204 No Content si se eliminó correctamente, 400 si no existe.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un aviso del día",
               description = "Elimina permanentemente el aviso. Usar desactivar si se quiere conservar el historial.")
    public ResponseEntity<?> eliminarAviso(
            @Parameter(description = "ID del aviso a eliminar") @PathVariable Long id) {
        try {
            avisoService.eliminarAviso(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error inesperado al eliminar el aviso."));
        }
    }

    /**
     * Ejecuta la limpieza automática de avisos expirados.
     *
     * Desactiva todos los avisos cuya fecha de expiración haya pasado.
     * Este endpoint puede llamarse manualmente desde el panel de secretaría
     * o configurarse para ejecutarse de forma programada.
     *
     * @return 200 OK con el número de avisos desactivados en el campo "desactivados".
     */
    @PostMapping("/limpiar-expirados")
    @Operation(summary = "Desactivar avisos cuya fecha de expiración haya pasado",
               description = "Operación de mantenimiento que archiva automáticamente los avisos caducados")
    public ResponseEntity<Map<String, Object>> limpiarAvisosExpirados() {
        int desactivados = avisoService.limpiarAvisosExpirados();
        return ResponseEntity.ok(Map.of(
                "desactivados", desactivados,
                "mensaje", desactivados > 0
                        ? desactivados + " aviso(s) expirado(s) desactivado(s) correctamente."
                        : "No había avisos expirados que desactivar."
        ));
    }

    // ─────────────────────────────────────────────────────────────
    // Endpoints de lectura (usuarios y secretaría)
    // ─────────────────────────────────────────────────────────────

    /**
     * Devuelve el historial completo de avisos (activos e inactivos).
     *
     * Usado por el panel de secretaría para mostrar todos los avisos
     * que han sido publicados, con su estado actual y campos de auditoría.
     *
     * @return 200 OK con la lista completa de avisos ordenada del más reciente.
     */
    @GetMapping
    @Operation(summary = "Listar todos los avisos del día",
               description = "Devuelve el historial completo de avisos, incluyendo inactivos y expirados")
    public ResponseEntity<List<AvisoDelDiaResponse>> listarTodos() {
        return ResponseEntity.ok(avisoService.listarTodosLosAvisos());
    }

    /**
     * Devuelve la lista de avisos marcados como activos.
     *
     * Usado por el panel de secretaría para ver qué avisos están
     * publicados actualmente (incluyendo los que pueden estar expirados
     * pero aún no han sido desactivados manualmente).
     *
     * @return 200 OK con la lista de avisos activos ordenada por prioridad.
     */
    @GetMapping("/activos")
    @Operation(summary = "Listar avisos activos",
               description = "Devuelve solo los avisos marcados como activos, sin filtrar por fecha de expiración")
    public ResponseEntity<List<AvisoDelDiaResponse>> listarActivos() {
        return ResponseEntity.ok(avisoService.listarAvisosActivos());
    }

    /**
     * Devuelve la lista de avisos activos Y vigentes a día de hoy.
     *
     * Este endpoint es el que debe llamar home.html al cargar para
     * saber qué avisos mostrar actualmente a los usuarios.
     * Filtra automáticamente los avisos cuya fecha de expiración ha pasado.
     *
     * @return 200 OK con la lista de avisos activos vigentes.
     */
    @GetMapping("/activos/vigentes")
    @Operation(summary = "Listar avisos activos y vigentes para mostrar a usuarios",
               description = "Filtra por activo=true y fechaExpiracion >= hoy. Usado por home.html.")
    public ResponseEntity<List<AvisoDelDiaResponse>> listarActivosVigentes() {
        return ResponseEntity.ok(avisoService.listarAvisosActivosParaUsuarios());
    }

    /**
     * Devuelve el aviso principal que debe mostrarse en el banner de home.html.
     *
     * Es el aviso activo, vigente, con mayor prioridad y más reciente.
     * Si no hay ningún aviso activo, devuelve 204 No Content para que
     * el frontend sepa que no debe mostrar ningún banner.
     *
     * Este es el endpoint más importante para la experiencia del usuario
     * final, ya que determina si aparece o no el banner informativo.
     *
     * @return 200 OK con el aviso principal, o 204 No Content si no hay ninguno.
     */
    @GetMapping("/activos/principal")
    @Operation(summary = "Obtener el aviso principal para mostrar en home.html",
               description = "Devuelve el aviso activo de mayor prioridad y más reciente. 204 si no hay ninguno.")
    public ResponseEntity<AvisoDelDiaResponse> obtenerAvisoPrincipal() {
        Optional<AvisoDelDiaResponse> aviso = avisoService.obtenerAvisoPrincipal();
        return aviso.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.noContent().build());
    }

    /**
     * Devuelve el detalle completo de un aviso concreto identificado por su ID.
     *
     * @param id ID del aviso a consultar.
     * @return 200 OK con el aviso, 400 si el ID no existe.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de un aviso por ID",
               description = "Devuelve todos los campos del aviso incluyendo auditoría y campo expirado calculado")
    public ResponseEntity<?> obtenerPorId(
            @Parameter(description = "ID del aviso a consultar") @PathVariable Long id) {
        try {
            return ResponseEntity.ok(avisoService.obtenerAvisoPorId(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Devuelve estadísticas básicas sobre los avisos activos.
     *
     * El panel de secretaría usa este endpoint para mostrar un badge
     * con la cantidad de avisos publicados actualmente.
     *
     * @return 200 OK con el campo "avisosActivosVigentes" con el contador.
     */
    @GetMapping("/stats")
    @Operation(summary = "Obtener estadísticas de avisos",
               description = "Devuelve el número de avisos activos y vigentes en este momento")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        long total = avisoService.contarAvisosActivosVigentes();
        return ResponseEntity.ok(Map.of(
                "avisosActivosVigentes", total,
                "hayAvisosActivos", total > 0
        ));
    }
}
