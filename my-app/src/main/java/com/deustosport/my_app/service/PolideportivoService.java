package com.deustosport.my_app.service;

import com.deustosport.my_app.dto.DisponibilidadPistaDTO;
import com.deustosport.my_app.dto.DisponibilidadPolideportivoDTO;
import com.deustosport.my_app.dto.ResumenSemanalPistaDTO;
import com.deustosport.my_app.dto.ResumenSemanalPolideportivoDTO;
import com.deustosport.my_app.entity.Polideportivo;
import com.deustosport.my_app.entity.Pista;
import com.deustosport.my_app.entity.Reserva;
import com.deustosport.my_app.enums.EstadoReserva;
import com.deustosport.my_app.repository.PolideportivoRepository;
import com.deustosport.my_app.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PolideportivoService {

    private final PolideportivoRepository polideportivoRepository;
    private final ReservaRepository reservaRepository;

    public PolideportivoService(PolideportivoRepository polideportivoRepository,
                                ReservaRepository reservaRepository) {
        this.polideportivoRepository = polideportivoRepository;
        this.reservaRepository = reservaRepository;
    }

    @Transactional(readOnly = true)
    public List<Polideportivo> obtenerTodos() {
        return polideportivoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Polideportivo> obtenerPorAyuntamiento(Long ayuntamientoId) {
        return polideportivoRepository.findByAyuntamientoId(ayuntamientoId);
    }

    @Transactional(readOnly = true)
    public Polideportivo obtenerPorId(Long polideportivoId) {
        return polideportivoRepository.findById(polideportivoId)
                .orElseThrow(() -> new IllegalArgumentException("Polideportivo no encontrado con ID: " + polideportivoId));
    }

    @Transactional(readOnly = true)
    public List<Pista> obtenerPistasByPolideportivo(Long polideportivoId) {
        Polideportivo polideportivo = polideportivoRepository.findById(polideportivoId)
                .orElseThrow(() -> new IllegalArgumentException("Polideportivo no encontrado"));
        return polideportivo.getPistas();
    }

    @Transactional
    public Polideportivo actualizarHorarioGeneral(Long polideportivoId, LocalTime horaApertura, LocalTime horaCierre) {
        Objects.requireNonNull(polideportivoId, "polideportivoId no puede ser null");
        if (horaApertura == null || horaCierre == null) {
            throw new IllegalArgumentException("La hora de apertura y cierre son obligatorias.");
        }

        if (!horaCierre.isAfter(horaApertura)) {
            throw new IllegalArgumentException("La hora de cierre debe ser posterior a la hora de apertura.");
        }

        Polideportivo polideportivo = polideportivoRepository.findById(polideportivoId)
                .orElseThrow(() -> new IllegalArgumentException("Polideportivo no encontrado con ID: " + polideportivoId));

        polideportivo.setHoraApertura(horaApertura);
        polideportivo.setHoraCierre(horaCierre);
        return polideportivoRepository.save(polideportivo);
    }

    @Transactional
    public Polideportivo crearPolideportivo(Polideportivo polideportivo) {
        if (polideportivoRepository.existsByNombreAndDireccionIgnoreCase(
                polideportivo.getNombre(), polideportivo.getDireccion())) {
            throw new IllegalArgumentException("Ya existe un polideportivo con el mismo nombre y dirección.");
        }
        return polideportivoRepository.save(polideportivo);
    }

    @Transactional
    public Polideportivo actualizarPolideportivo(Long id, String nombre, String direccion) {
        Polideportivo poli = polideportivoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Polideportivo no encontrado"));

        if (polideportivoRepository.existsByNombreAndDireccionIgnoreCaseExcludingId(id, nombre, direccion)) {
            throw new IllegalArgumentException("Ya existe otro polideportivo con el mismo nombre y dirección.");
        }

        poli.setNombre(nombre);
        poli.setDireccion(direccion);
        return polideportivoRepository.save(poli);
    }

    @Transactional(readOnly = true)
    public ResumenSemanalPolideportivoDTO obtenerResumenSemanal(Long polideportivoId) {
        Objects.requireNonNull(polideportivoId, "polideportivoId no puede ser null");
        Polideportivo poli = polideportivoRepository.findById(polideportivoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Polideportivo no encontrado con ID: " + polideportivoId));

        LocalDate hoy = LocalDate.now();
        LocalDate inicioSemana = hoy.minusDays(hoy.getDayOfWeek().getValue() - 1);
        LocalDate finSemana = inicioSemana.plusDays(6);

        long horasPorDia = calcularHorasDisponibles(poli);
        List<Pista> pistas = poli.getPistas();
        List<ResumenSemanalPistaDTO> detalles = new ArrayList<>();

        long totalReservasSemana = 0;
        long totalConfirmadas = 0;
        long totalCanceladas = 0;
        BigDecimal ingresosTotales = BigDecimal.ZERO;

        for (Pista pista : pistas) {
            List<Reserva> reservasSemana = reservaRepository.findActivasByPistaAndRango(
                    pista.getId(), inicioSemana, finSemana);

            long confirmadas = reservasSemana.stream()
                    .filter(r -> r.getEstado() == EstadoReserva.CONFIRMADA
                              || r.getEstado() == EstadoReserva.COMPLETADA)
                    .count();
            long canceladas = reservasSemana.stream()
                    .filter(r -> r.getEstado() == EstadoReserva.CANCELADA)
                    .count();

            BigDecimal ingresosPista = reservasSemana.stream()
                    .filter(r -> r.getEstado() == EstadoReserva.CONFIRMADA
                              || r.getEstado() == EstadoReserva.COMPLETADA)
                    .map(Reserva::getPrecioTotal)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            long horasReservadas = reservasSemana.stream()
                    .filter(r -> r.getEstado() != EstadoReserva.CANCELADA)
                    .mapToLong(r -> Duration.between(r.getHoraInicio(), r.getHoraFin()).toHours())
                    .sum();
            long horasDisponibles = horasPorDia * 7;
            double tasa = horasDisponibles > 0
                    ? Math.round((double) horasReservadas / horasDisponibles * 10000.0) / 100.0
                    : 0.0;

            ResumenSemanalPistaDTO dto = new ResumenSemanalPistaDTO();
            dto.setPistaId(pista.getId());
            dto.setPistaNombre(pista.getNombre());
            dto.setTipoDeporte(pista.getTipoDeporte());
            dto.setActiva(pista.isActiva());
            dto.setTotalReservasSemana(reservasSemana.size());
            dto.setReservasConfirmadas(confirmadas);
            dto.setReservasCanceladas(canceladas);
            dto.setIngresosSemana(ingresosPista);
            dto.setHorasReservadas(horasReservadas);
            dto.setHorasDisponibles(horasDisponibles);
            dto.setTasaOcupacion(tasa);
            detalles.add(dto);

            totalReservasSemana += reservasSemana.size();
            totalConfirmadas += confirmadas;
            totalCanceladas += canceladas;
            ingresosTotales = ingresosTotales.add(ingresosPista);
        }

        ResumenSemanalPolideportivoDTO resultado = new ResumenSemanalPolideportivoDTO();
        resultado.setPolideportivoId(poli.getId());
        resultado.setPolideportivoNombre(poli.getNombre());
        resultado.setDireccion(poli.getDireccion());
        resultado.setInicioSemana(inicioSemana);
        resultado.setFinSemana(finSemana);
        resultado.setTotalPistas(pistas.size());
        resultado.setPistasActivas((int) pistas.stream().filter(Pista::isActiva).count());
        resultado.setTotalReservasSemana(totalReservasSemana);
        resultado.setReservasConfirmadasSemana(totalConfirmadas);
        resultado.setReservasCanceladasSemana(totalCanceladas);
        resultado.setIngresosTotalesSemana(ingresosTotales);
        resultado.setDetallesPorPista(detalles);
        return resultado;
    }

    private long calcularHorasDisponibles(Polideportivo poli) {
        if (poli.getHoraApertura() == null || poli.getHoraCierre() == null) {
            return 12L;
        }
        return Duration.between(poli.getHoraApertura(), poli.getHoraCierre()).toHours();
    }

    @Transactional(readOnly = true)
    public DisponibilidadPolideportivoDTO obtenerDisponibilidadPolideportivo(Long polideportivoId, LocalDate fecha) {
        Objects.requireNonNull(polideportivoId, "polideportivoId no puede ser null");
        Objects.requireNonNull(fecha, "fecha no puede ser null");

        Polideportivo polideportivo = polideportivoRepository.findById(polideportivoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Polideportivo no encontrado con ID: " + polideportivoId));

        List<Pista> todasLasPistas = polideportivo.getPistas();

        List<DisponibilidadPistaDTO> pistasDto = todasLasPistas.stream().map(pista -> {
            DisponibilidadPistaDTO dto = new DisponibilidadPistaDTO();
            dto.setPistaId(pista.getId());
            dto.setPistaNombre(pista.getNombre());
            dto.setTipoDeporte(pista.getTipoDeporte());
            dto.setMaxJugadores(pista.getMaxJugadores());
            dto.setActiva(pista.isActiva());

            List<Reserva> reservasDia = reservaRepository.findActivasByPistaAndRango(
                    pista.getId(), fecha, fecha);

            List<Map<String, String>> slots = reservasDia.stream().map(r -> {
                Map<String, String> slot = new LinkedHashMap<>();
                slot.put("horaInicio", r.getHoraInicio().toString());
                slot.put("horaFin", r.getHoraFin().toString());
                slot.put("estado", r.getEstado().name());
                return slot;
            }).collect(Collectors.toList());

            dto.setSlotsOcupados(slots);
            return dto;
        }).collect(Collectors.toList());

        DisponibilidadPolideportivoDTO resultado = new DisponibilidadPolideportivoDTO();
        resultado.setPolideportivoId(polideportivo.getId());
        resultado.setPolideportivoNombre(polideportivo.getNombre());
        resultado.setDireccion(polideportivo.getDireccion());
        resultado.setHoraApertura(polideportivo.getHoraApertura() != null
                ? polideportivo.getHoraApertura().toString() : "08:00");
        resultado.setHoraCierre(polideportivo.getHoraCierre() != null
                ? polideportivo.getHoraCierre().toString() : "22:00");
        resultado.setFecha(fecha);
        resultado.setTotalPistas(todasLasPistas.size());
        resultado.setPistasActivas((int) todasLasPistas.stream().filter(Pista::isActiva).count());
        resultado.setPistas(pistasDto);
        return resultado;
    }

    @Transactional
    public void eliminarPolideportivo(Long id) {
        Objects.requireNonNull(id, "id no puede ser null");
        Polideportivo polideportivo = polideportivoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Polideportivo no encontrado con ID: " + id));

        List<Long> pistaIds = polideportivo.getPistas().stream()
                .map(Pista::getId)
                .collect(Collectors.toList());

        if (!pistaIds.isEmpty()) {
            long reservasActivas = reservaRepository.countActivasFuturasByPistaIds(pistaIds, LocalDate.now());
            if (reservasActivas > 0) {
                throw new IllegalStateException(
                        "No se puede eliminar el polideportivo porque tiene " + reservasActivas
                        + " reserva(s) activa(s). Cancele las reservas antes de eliminar el polideportivo.");
            }
        }

        polideportivoRepository.delete(polideportivo);
    }
}
