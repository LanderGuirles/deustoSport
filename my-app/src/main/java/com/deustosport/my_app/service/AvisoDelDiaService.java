package com.deustosport.my_app.service;

import com.deustosport.my_app.dto.AvisoDelDiaRequest;
import com.deustosport.my_app.dto.AvisoDelDiaResponse;
import com.deustosport.my_app.entity.AvisoDelDia;
import com.deustosport.my_app.enums.TipoAviso;
import com.deustosport.my_app.repository.AvisoDelDiaRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de aplicación para la gestión de avisos del día.
 *
 * Centraliza toda la lógica de negocio relacionada con la creación,
 * modificación, activación, desactivación, consulta y eliminación de
 * avisos del día publicados por el personal de secretaría.
 *
 * Los avisos del día son mensajes cortos que aparecen en la pantalla de
 * inicio (home.html) de todos los usuarios para informarles de incidencias
 * o novedades que afecten al uso de las polideportivos del polideportivo.
 *
 * Reglas de negocio que aplica este servicio:
 * <ul>
 *   <li>Un aviso sin título o mensaje vacío es rechazado con excepción.</li>
 *   <li>Si no se especifica tipo, se asigna AVISO por defecto.</li>
 *   <li>Si no se especifica prioridad, se asigna 0 por defecto.</li>
 *   <li>Un aviso con fechaExpiracion anterior a hoy se considera expirado
 *       y se excluye de las consultas de avisos activos para usuarios.</li>
 *   <li>Los avisos se crean activos por defecto.</li>
 *   <li>Al actualizar un aviso se registra la fecha y el nombre del
 *       editor en los campos de auditoría correspondientes.</li>
 * </ul>
 */
@Service
public class AvisoDelDiaService {

    private final AvisoDelDiaRepository avisoRepository;

    public AvisoDelDiaService(AvisoDelDiaRepository avisoRepository) {
        this.avisoRepository = avisoRepository;
    }

    // ─────────────────────────────────────────────────────────────
    // Creación
    // ─────────────────────────────────────────────────────────────

    /**
     * Publica un nuevo aviso del día.
     *
     * Valida los campos obligatorios, aplica valores por defecto para
     * los opcionales y persiste la entidad. El aviso se crea activo
     * para que aparezca de inmediato en la pantalla de inicio de los
     * usuarios que accedan después de la publicación.
     *
     * @param request DTO con los datos del aviso a publicar.
     * @return DTO de respuesta con el aviso persistido y su ID asignado.
     * @throws IllegalArgumentException si el título o el mensaje están vacíos.
     */
    @Transactional
    public AvisoDelDiaResponse publicarAviso(AvisoDelDiaRequest request) {

        validarCamposObligatorios(request);

        AvisoDelDia aviso = new AvisoDelDia();
        aviso.setTitulo(request.getTitulo().trim());
        aviso.setMensaje(request.getMensaje().trim());
        aviso.setTipo(request.getTipo() != null ? request.getTipo() : TipoAviso.AVISO);
        aviso.setActivo(true);
        aviso.setFechaCreacion(LocalDateTime.now());
        aviso.setFechaExpiracion(request.getFechaExpiracion());
        aviso.setPrioridad(request.getPrioridad() != null ? request.getPrioridad() : 0);
        aviso.setCreadoPorId(request.getCreadoPorId());
        aviso.setCreadoPorNombre(request.getCreadoPorNombre());
        aviso.setFechaUltimaModificacion(null);
        aviso.setModificadoPorNombre(null);

        AvisoDelDia guardado = avisoRepository.save(aviso);
        return toDto(guardado);
    }

    // ─────────────────────────────────────────────────────────────
    // Consultas para usuarios (home.html)
    // ─────────────────────────────────────────────────────────────

    /**
     * Devuelve el aviso activo de mayor prioridad y más reciente que
     * esté vigente a día de hoy. Es el aviso principal que se muestra
     * en el banner de la pantalla de inicio de los usuarios.
     *
     * Antes de consultar, ejecuta la limpieza automática de avisos
     * expirados para garantizar que los datos están al día.
     *
     * @return Optional con el aviso principal, vacío si no hay ninguno activo.
     */
    @Transactional(readOnly = true)
    public Optional<AvisoDelDiaResponse> obtenerAvisoPrincipal() {
        LocalDate hoy = LocalDate.now();
        return avisoRepository.findAvisoPrincipal(hoy).map(this::toDto);
    }

    /**
     * Devuelve la lista completa de avisos activos y vigentes a día de
     * hoy, ordenados por prioridad descendente y fecha de creación
     * descendente. Se usa cuando el frontend necesita mostrar todos los
     * avisos activos (por ejemplo, en un panel expandible).
     *
     * @return lista de avisos activos vigentes, posiblemente vacía.
     */
    @Transactional(readOnly = true)
    public List<AvisoDelDiaResponse> listarAvisosActivosParaUsuarios() {
        LocalDate hoy = LocalDate.now();
        return avisoRepository.findAvisosActivosVigentes(hoy)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────
    // Consultas para secretaría (panel de administración)
    // ─────────────────────────────────────────────────────────────

    /**
     * Devuelve el historial completo de avisos, incluyendo activos,
     * inactivos y expirados, ordenados del más reciente al más antiguo.
     * Se usa en el panel de secretaría para mostrar todos los avisos
     * que han sido publicados alguna vez.
     *
     * @return lista completa de avisos en orden cronológico inverso.
     */
    @Transactional(readOnly = true)
    public List<AvisoDelDiaResponse> listarTodosLosAvisos() {
        return avisoRepository.findAllByOrderByFechaCreacionDesc()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Devuelve únicamente los avisos marcados como activos, sin filtrar
     * por fecha de expiración. El panel de secretaría los muestra con
     * un indicador visual diferente según estén vigentes o expirados.
     *
     * @return lista de avisos activos, ordenados por prioridad y fecha.
     */
    @Transactional(readOnly = true)
    public List<AvisoDelDiaResponse> listarAvisosActivos() {
        return avisoRepository.findByActivoTrueOrderByPrioridadDescFechaCreacionDesc()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Devuelve únicamente los avisos desactivados/archivados, ordenados
     * por fecha de creación descendente.
     *
     * @return lista de avisos inactivos.
     */
    @Transactional(readOnly = true)
    public List<AvisoDelDiaResponse> listarAvisosInactivos() {
        return avisoRepository.findByActivoFalseOrderByFechaCreacionDesc()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Devuelve el número de avisos activos y vigentes a día de hoy.
     * El panel de secretaría lo usa para mostrar un badge con la
     * cantidad de avisos publicados actualmente.
     *
     * @return número de avisos activos vigentes.
     */
    @Transactional(readOnly = true)
    public long contarAvisosActivosVigentes() {
        return avisoRepository.countAvisosActivosVigentes(LocalDate.now());
    }

    /**
     * Busca y devuelve un aviso concreto por su ID.
     *
     * @param id identificador del aviso.
     * @return DTO del aviso encontrado.
     * @throws IllegalArgumentException si no existe ningún aviso con ese ID.
     */
    @Transactional(readOnly = true)
    public AvisoDelDiaResponse obtenerAvisoPorId(Long id) {
        AvisoDelDia aviso = buscarPorIdOFallar(id);
        return toDto(aviso);
    }

    // ─────────────────────────────────────────────────────────────
    // Modificación
    // ─────────────────────────────────────────────────────────────

    /**
     * Actualiza los campos editables de un aviso existente.
     *
     * Registra la fecha de modificación y el nombre del editor en los
     * campos de auditoría. No modifica el campo activo ni la fecha de
     * creación original del aviso.
     *
     * @param id      ID del aviso a actualizar.
     * @param request DTO con los nuevos valores.
     * @return DTO del aviso actualizado.
     * @throws IllegalArgumentException si el aviso no existe o los
     *                                  campos obligatorios están vacíos.
     */
    @Transactional
    public AvisoDelDiaResponse actualizarAviso(Long id, AvisoDelDiaRequest request) {

        validarCamposObligatorios(request);

        AvisoDelDia aviso = buscarPorIdOFallar(id);

        aviso.setTitulo(request.getTitulo().trim());
        aviso.setMensaje(request.getMensaje().trim());
        aviso.setTipo(request.getTipo() != null ? request.getTipo() : aviso.getTipo());
        aviso.setFechaExpiracion(request.getFechaExpiracion());
        aviso.setPrioridad(request.getPrioridad() != null ? request.getPrioridad() : aviso.getPrioridad());
        aviso.setFechaUltimaModificacion(LocalDateTime.now());
        aviso.setModificadoPorNombre(request.getCreadoPorNombre());

        return toDto(avisoRepository.save(aviso));
    }

    // ─────────────────────────────────────────────────────────────
    // Activación / desactivación
    // ─────────────────────────────────────────────────────────────

    /**
     * Activa un aviso para que aparezca en la pantalla de inicio de
     * los usuarios. Si el aviso ya estaba activo, la operación no
     * produce ningún cambio visible.
     *
     * @param id ID del aviso a activar.
     * @return DTO del aviso con el campo activo = true.
     * @throws IllegalArgumentException si el aviso no existe.
     */
    @Transactional
    public AvisoDelDiaResponse activarAviso(Long id) {
        AvisoDelDia aviso = buscarPorIdOFallar(id);
        aviso.setActivo(true);
        return toDto(avisoRepository.save(aviso));
    }

    /**
     * Desactiva un aviso para que deje de mostrarse en la pantalla de
     * inicio de los usuarios. El aviso permanece en base de datos y
     * puede reactivarse en cualquier momento.
     *
     * @param id ID del aviso a desactivar.
     * @return DTO del aviso con el campo activo = false.
     * @throws IllegalArgumentException si el aviso no existe.
     */
    @Transactional
    public AvisoDelDiaResponse desactivarAviso(Long id) {
        AvisoDelDia aviso = buscarPorIdOFallar(id);
        aviso.setActivo(false);
        return toDto(avisoRepository.save(aviso));
    }

    // ─────────────────────────────────────────────────────────────
    // Eliminación
    // ─────────────────────────────────────────────────────────────

    /**
     * Elimina permanentemente un aviso de la base de datos.
     *
     * Esta operación es irreversible. Para ocultar un aviso sin
     * eliminarlo, usar {@link #desactivarAviso(Long)} en su lugar.
     *
     * @param id ID del aviso a eliminar.
     * @throws IllegalArgumentException si el aviso no existe.
     */
    @Transactional
    public void eliminarAviso(Long id) {
        if (!avisoRepository.existsById(id)) {
            throw new IllegalArgumentException(
                "No existe ningún aviso con ID " + id + ". No se puede eliminar.");
        }
        avisoRepository.deleteById(id);
    }

    // ─────────────────────────────────────────────────────────────
    // Mantenimiento automático
    // ─────────────────────────────────────────────────────────────

    /**
     * Desactiva todos los avisos cuya fecha de expiración haya sido
     * superada. Esta operación es idempotente y puede ejecutarse en
     * cualquier momento sin efectos secundarios.
     *
     * @return número de avisos desactivados en esta ejecución.
     */
    @Transactional
    public int limpiarAvisosExpirados() {
        return avisoRepository.desactivarAvisosExpirados(LocalDate.now());
    }

    // ─────────────────────────────────────────────────────────────
    // Métodos privados de soporte
    // ─────────────────────────────────────────────────────────────

    /**
     * Busca un aviso por ID y lanza excepción si no existe.
     *
     * @param id identificador del aviso.
     * @return entidad AvisoDelDia encontrada.
     * @throws IllegalArgumentException si no se encuentra.
     */
    private AvisoDelDia buscarPorIdOFallar(Long id) {
        return avisoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                    "No se encontró ningún aviso con ID " + id));
    }

    /**
     * Valida que los campos obligatorios del request no sean nulos ni vacíos.
     *
     * @param request DTO de entrada a validar.
     * @throws IllegalArgumentException si algún campo obligatorio falta.
     */
    private void validarCamposObligatorios(AvisoDelDiaRequest request) {
        if (request.getTitulo() == null || request.getTitulo().isBlank()) {
            throw new IllegalArgumentException("El título del aviso no puede estar vacío.");
        }
        if (request.getMensaje() == null || request.getMensaje().isBlank()) {
            throw new IllegalArgumentException("El mensaje del aviso no puede estar vacío.");
        }
        if (request.getTitulo().trim().length() > 120) {
            throw new IllegalArgumentException(
                "El título del aviso no puede superar los 120 caracteres.");
        }
        if (request.getMensaje().trim().length() > 1000) {
            throw new IllegalArgumentException(
                "El mensaje del aviso no puede superar los 1000 caracteres.");
        }
        if (request.getPrioridad() != null && (request.getPrioridad() < 0 || request.getPrioridad() > 10)) {
            throw new IllegalArgumentException(
                "La prioridad debe estar entre 0 y 10.");
        }
    }

    /**
     * Convierte una entidad AvisoDelDia en su DTO de respuesta.
     *
     * Calcula el campo {@code expirado} comparando la fecha de expiración
     * con la fecha actual en el momento de la conversión.
     *
     * @param a entidad a convertir.
     * @return DTO de respuesta listo para serializar.
     */
    private AvisoDelDiaResponse toDto(AvisoDelDia a) {
        boolean expirado = a.getFechaExpiracion() != null
                && a.getFechaExpiracion().isBefore(LocalDate.now());

        return new AvisoDelDiaResponse(
                a.getId(),
                a.getTitulo(),
                a.getMensaje(),
                a.getTipo(),
                a.isActivo(),
                a.getFechaCreacion(),
                a.getFechaExpiracion(),
                a.getPrioridad(),
                a.getCreadoPorNombre(),
                a.getCreadoPorId(),
                a.getFechaUltimaModificacion(),
                a.getModificadoPorNombre(),
                expirado
        );
    }
}
