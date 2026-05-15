package com.deustosport.my_app.service;

import com.deustosport.my_app.entity.EstadoIluminacion;
import com.deustosport.my_app.entity.Pista;
import com.deustosport.my_app.entity.Reserva;
import com.deustosport.my_app.enums.EstadoReserva;
import com.deustosport.my_app.repository.EstadoIluminacionRepository;
import com.deustosport.my_app.repository.PistaRepository;
import com.deustosport.my_app.repository.ReservaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio de gestión automática de iluminación de pistas deportivas.
 *
 * Reglas:
 * - Encender la luz 5 minutos ANTES de que comience una reserva confirmada.
 * - Apagar la luz 5 minutos DESPUÉS de que termine la última reserva.
 * - Si hay reservas consecutivas (la siguiente empieza cuando termina la actual),
 *   la luz permanece encendida sin interrupción.
 */
@Slf4j
@Service
public class IluminacionService {

    private static final int MINUTOS_ANTICIPACION_ENCENDIDO = 5;
    private static final int MINUTOS_GRACIA_APAGADO = 5;

    private final EstadoIluminacionRepository estadoRepo;
    private final PistaRepository pistaRepository;
    private final ReservaRepository reservaRepository;

    public IluminacionService(EstadoIluminacionRepository estadoRepo,
                              PistaRepository pistaRepository,
                              ReservaRepository reservaRepository) {
        this.estadoRepo = estadoRepo;
        this.pistaRepository = pistaRepository;
        this.reservaRepository = reservaRepository;
    }

    // ═══════════════════════════════════════════
    //  REVISIÓN PERIÓDICA (llamada por el scheduler)
    // ═══════════════════════════════════════════

    /**
     * Revisa todas las pistas activas y decide si encender o apagar la iluminación.
     * Ejecutado periódicamente por IluminacionScheduler.
     */
    @Transactional
    public void revisarIluminacion() {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDate hoy = ahora.toLocalDate();
        LocalTime horaActual = ahora.toLocalTime();

        List<Pista> pistasActivas = pistaRepository.findByActivaTrue();

        for (Pista pista : pistasActivas) {
            EstadoIluminacion estado = obtenerOCrearEstado(pista);

            // Buscar reservas confirmadas de hoy para esta pista
            List<Reserva> reservasHoy = reservaRepository.findActivasByPistaAndRango(
                    pista.getId(), hoy, hoy);

            // Filtrar solo las CONFIRMADAS (no canceladas ni pendientes)
            List<Reserva> confirmadas = reservasHoy.stream()
                    .filter(r -> r.getEstado() == EstadoReserva.CONFIRMADA)
                    .toList();

            if (confirmadas.isEmpty()) {
                // No hay reservas hoy: si la luz está encendida, apagarla
                if (estado.isEncendida()) {
                    apagarLuz(estado, "Sin reservas confirmadas hoy en " + pista.getNombre());
                }
                continue;
            }

            // ── ¿Hay alguna reserva que requiera encender la luz? ──
            // La luz debe estar encendida si estamos dentro de la ventana:
            //   [horaInicio - 5min,  horaFin + 5min]
            // teniendo en cuenta reservas consecutivas.

            boolean debeEstarEncendida = false;
            String motivoEncendido = null;

            for (Reserva r : confirmadas) {
                LocalTime encenderDesde = r.getHoraInicio().minusMinutes(MINUTOS_ANTICIPACION_ENCENDIDO);
                LocalTime apagarHasta = r.getHoraFin().plusMinutes(MINUTOS_GRACIA_APAGADO);

                // ¿Estamos en la ventana de esta reserva?
                if (!horaActual.isBefore(encenderDesde) && horaActual.isBefore(apagarHasta)) {
                    debeEstarEncendida = true;
                    motivoEncendido = "Reserva activa/próxima en " + pista.getNombre()
                            + " (" + r.getHoraInicio() + " - " + r.getHoraFin() + ")";
                    break;
                }
            }

            // Comprobación adicional: si estamos en la gracia post-reserva,
            // verificar si hay reserva consecutiva
            if (!debeEstarEncendida) {
                for (Reserva r : confirmadas) {
                    LocalTime finGracia = r.getHoraFin().plusMinutes(MINUTOS_GRACIA_APAGADO);

                    // Estamos en los 5 min de gracia post-reserva
                    if (!horaActual.isBefore(r.getHoraFin()) && horaActual.isBefore(finGracia)) {
                        // Verificar si hay reserva consecutiva
                        boolean consecutiva = tieneReservaConsecutiva(pista.getId(), hoy, r.getHoraFin(), confirmadas);
                        if (consecutiva) {
                            debeEstarEncendida = true;
                            motivoEncendido = "Reserva consecutiva detectada en " + pista.getNombre()
                                    + " tras finalizar a las " + r.getHoraFin();
                            break;
                        }
                    }
                }
            }

            // También encender si una reserva comienza pronto (dentro de 5 min)
            if (!debeEstarEncendida) {
                for (Reserva r : confirmadas) {
                    LocalTime encenderDesde = r.getHoraInicio().minusMinutes(MINUTOS_ANTICIPACION_ENCENDIDO);
                    if (!horaActual.isBefore(encenderDesde) && horaActual.isBefore(r.getHoraInicio())) {
                        debeEstarEncendida = true;
                        motivoEncendido = "Encendido anticipado para reserva en " + pista.getNombre()
                                + " que comienza a las " + r.getHoraInicio();
                        break;
                    }
                }
            }

            // ── Aplicar decisión ──
            if (debeEstarEncendida && !estado.isEncendida()) {
                encenderLuz(estado, motivoEncendido);
            } else if (!debeEstarEncendida && estado.isEncendida()) {
                // Determinar motivo del apagado
                String motivoApagado = determinarMotivoApagado(pista, confirmadas, horaActual);
                apagarLuz(estado, motivoApagado);
            }
        }
    }

    // ═══════════════════════════════════════════
    //  ENCENDER / APAGAR
    // ═══════════════════════════════════════════

    private void encenderLuz(EstadoIluminacion estado, String motivo) {
        estado.setEncendida(true);
        estado.setUltimoCambio(LocalDateTime.now());
        estado.setMotivoUltimoCambio(motivo);
        estadoRepo.save(estado);
        log.info("💡 LUZ ENCENDIDA — {} | {}", estado.getPista().getNombre(), motivo);
    }

    private void apagarLuz(EstadoIluminacion estado, String motivo) {
        estado.setEncendida(false);
        estado.setUltimoCambio(LocalDateTime.now());
        estado.setMotivoUltimoCambio(motivo);
        estadoRepo.save(estado);
        log.info("🌑 LUZ APAGADA — {} | {}", estado.getPista().getNombre(), motivo);
    }

    // ═══════════════════════════════════════════
    //  RESERVAS CONSECUTIVAS
    // ═══════════════════════════════════════════

    /**
     * Comprueba si hay una reserva confirmada que comience exactamente cuando
     * termina la reserva actual (horaFin de la actual == horaInicio de la siguiente).
     */
    private boolean tieneReservaConsecutiva(Long pistaId, LocalDate fecha,
                                            LocalTime horaFinActual,
                                            List<Reserva> reservasConfirmadas) {
        return reservasConfirmadas.stream()
                .anyMatch(r -> r.getHoraInicio().equals(horaFinActual));
    }

    private String determinarMotivoApagado(Pista pista, List<Reserva> confirmadas, LocalTime horaActual) {
        // Encontrar la última reserva que ya terminó
        return confirmadas.stream()
                .filter(r -> !horaActual.isBefore(r.getHoraFin()))
                .reduce((a, b) -> a.getHoraFin().isAfter(b.getHoraFin()) ? a : b)
                .map(r -> "Fin de reserva en " + pista.getNombre()
                        + " (terminó a las " + r.getHoraFin() + ") + "
                        + MINUTOS_GRACIA_APAGADO + " min de gracia. Sin reservas consecutivas.")
                .orElse("Sin reservas activas en " + pista.getNombre());
    }

    // ═══════════════════════════════════════════
    //  CONSULTAS PÚBLICAS
    // ═══════════════════════════════════════════

    /**
     * Obtiene el estado de iluminación de una pista concreta.
     */
    @Transactional(readOnly = true)
    public EstadoIluminacion obtenerEstado(Long pistaId) {
        Pista pista = pistaRepository.findById(pistaId)
                .orElseThrow(() -> new IllegalArgumentException("Pista no encontrada"));
        return obtenerOCrearEstado(pista);
    }

    /**
     * Lista el estado de iluminación de todas las pistas activas.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> obtenerTodosLosEstados() {
        List<Pista> pistasActivas = pistaRepository.findByActivaTrue();

        return pistasActivas.stream().map(pista -> {
            EstadoIluminacion estado = obtenerOCrearEstado(pista);
            return Map.<String, Object>of(
                    "pistaId", pista.getId(),
                    "pistaNombre", pista.getNombre(),
                    "tipoDeporte", pista.getTipoDeporte().name(),
                    "encendida", estado.isEncendida(),
                    "ultimoCambio", estado.getUltimoCambio() != null
                            ? estado.getUltimoCambio().toString() : "",
                    "motivo", estado.getMotivoUltimoCambio() != null
                            ? estado.getMotivoUltimoCambio() : "Sin actividad registrada"
            );
        }).collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════

    /**
     * Obtiene el estado de iluminación de una pista, creándolo si no existe.
     */
    private EstadoIluminacion obtenerOCrearEstado(Pista pista) {
        return estadoRepo.findByPistaId(pista.getId())
                .orElseGet(() -> {
                    EstadoIluminacion nuevo = new EstadoIluminacion();
                    nuevo.setPista(pista);
                    nuevo.setEncendida(false);
                    nuevo.setUltimoCambio(LocalDateTime.now());
                    nuevo.setMotivoUltimoCambio("Estado inicial — luz apagada");
                    return estadoRepo.save(nuevo);
                });
    }
}
