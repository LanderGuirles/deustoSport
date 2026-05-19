package com.deustosport.my_app.service;

import com.deustosport.my_app.dto.FestivoRequest;
import com.deustosport.my_app.dto.FestivoResponse;
import com.deustosport.my_app.entity.Festivo;
import com.deustosport.my_app.entity.Reserva;
import com.deustosport.my_app.repository.FestivoRepository;
import com.deustosport.my_app.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FestivoService {

    private final FestivoRepository festivoRepository;
    private final ReservaRepository reservaRepository;
    private final ReservaService reservaService;

    public FestivoService(FestivoRepository festivoRepository, 
                          ReservaRepository reservaRepository,
                          ReservaService reservaService) {
        this.festivoRepository = festivoRepository;
        this.reservaRepository = reservaRepository;
        this.reservaService = reservaService;
    }

    @Transactional
    public FestivoResponse programarFestivo(FestivoRequest request) {
        if (request.getFechaFin().isBefore(request.getFechaInicio())) {
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la de inicio.");
        }

        Festivo festivo = new Festivo();
        festivo.setNombre(request.getNombre());
        festivo.setFechaInicio(request.getFechaInicio());
        festivo.setFechaFin(request.getFechaFin());
        Festivo guardado = festivoRepository.save(festivo);

        // Cancelar reservas existentes en ese periodo usando consulta acotada por fechas
        List<Reserva> reservasConflictivas = reservaRepository.findActivasByRango(
                request.getFechaInicio(), request.getFechaFin());

        for (Reserva r : reservasConflictivas) {
            reservaService.cancelarReservaPorBloqueo(r.getId());
        }

        return toDto(guardado);
    }

    @Transactional(readOnly = true)
    public List<FestivoResponse> listarFestivos() {
        return festivoRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void eliminarFestivo(Long id) {
        festivoRepository.deleteById(id);
    }

    public boolean esFechaFestiva(LocalDate fecha) {
        return !festivoRepository.findFestivosEnFecha(fecha).isEmpty();
    }

    private FestivoResponse toDto(Festivo f) {
        return new FestivoResponse(f.getId(), f.getNombre(), f.getFechaInicio(), f.getFechaFin());
    }
}
