package com.deustosport.my_app.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.deustosport.my_app.dto.PistaRequest;
import com.deustosport.my_app.dto.PistaResponse;
import com.deustosport.my_app.entity.Polideportivo;
import com.deustosport.my_app.entity.Pista;
import com.deustosport.my_app.repository.PolideportivoRepository;
import com.deustosport.my_app.repository.PistaRepository;
import com.deustosport.my_app.repository.ReservaRepository;
import com.deustosport.my_app.entity.Reserva;
import org.springframework.context.annotation.Lazy;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PistaService {

    private final PistaRepository pistaRepository;
    private final PolideportivoRepository polideportivoRepository;
    private final ReservaRepository reservaRepository;
    private final ReservaService reservaService;

    public PistaService(PistaRepository pistaRepository, 
                        PolideportivoRepository polideportivoRepository,
                        ReservaRepository reservaRepository,
                        @Lazy ReservaService reservaService) {
        this.pistaRepository = pistaRepository;
        this.polideportivoRepository = polideportivoRepository;
        this.reservaRepository = reservaRepository;
        this.reservaService = reservaService;
    }


    // metodo para mapear entity a dto para uso interno del service
    private PistaResponse convertirPistaToDto(Pista pista) {
        PistaResponse pistaResponseDto = new PistaResponse();
        pistaResponseDto.setId(pista.getId());
        pistaResponseDto.setNombre(pista.getNombre());
        pistaResponseDto.setTipoDeporte(pista.getTipoDeporte());
        pistaResponseDto.setMaxJugadores(pista.getMaxJugadores());
        pistaResponseDto.setActiva(pista.isActiva());

        if (pista.getPolideportivo() != null) {
            pistaResponseDto.setPolideportivoId(pista.getPolideportivo().getId());
            pistaResponseDto.setPolideportivoNombre(pista.getPolideportivo().getNombre());
        }

        return pistaResponseDto;
    }

    @Transactional
    public List<Pista> obtenerPistasPorPolideportivoId(Long polideportivoId){
        Objects.requireNonNull(polideportivoId, "polideportivoId no puede ser null");
        if (!polideportivoRepository.existsById(polideportivoId)) {
            throw new RuntimeException("No se puede listar: El polideportivo con ID " + polideportivoId + " no existe.");
        }
        return pistaRepository.findByPolideportivoId(polideportivoId);
    }


    @Transactional
    public List<PistaResponse> obtenerTodasLasPistas() {
        List<Pista> pistas = pistaRepository.findAll();

        // transformar la lista de pistas a pistaDto
        return pistas.stream().map(this::convertirPistaToDto).collect(Collectors.toList());
    }

    @Transactional
    public PistaResponse obtenerPistaPorId(Long id) {
        Objects.requireNonNull(id, "id no puede ser null");
        Pista pista = pistaRepository.findById(id).orElseThrow(() -> new RuntimeException("La pista con ID " + id + " no existe."));
        return convertirPistaToDto(pista);
    }

    @Transactional
    public Pista registrarNuevaPista(Pista nuevaPista) {

        nuevaPista.setId(null); 

        // Comprobar que no haya una pista registrada con ese nombre
        if (pistaRepository.existsByNombreIgnoreCase(nuevaPista.getNombre())) {
            throw new RuntimeException("Ya existe una pista con ese nombre.");
        }

        // Comprobar que venga el ID del polideportivo
        if (nuevaPista.getPolideportivo() == null || nuevaPista.getPolideportivo().getId() == null) {
            throw new RuntimeException("Error: La pista debe estar vinculada a un polideportivo válido.");
        }

        Long polideportivoId = Objects.requireNonNull(nuevaPista.getPolideportivo().getId(), "polideportivoId no puede ser null");
        
        // Buscamos el polideportivo real
        Polideportivo polideportivoAsociado = polideportivoRepository.findById(polideportivoId)
                .orElseThrow(() -> new RuntimeException("Error: No se encontró el polideportivo con ID: " + polideportivoId));

        nuevaPista.setPolideportivo(polideportivoAsociado);
        nuevaPista.setActiva(true);

        return pistaRepository.save(nuevaPista);
    }


    @Transactional
    public PistaResponse actualizarPista(Long id, PistaRequest pistaRequestDTO) {
        Objects.requireNonNull(id, "id no puede ser null");
        Pista pistaExistente = pistaRepository.findById(id).orElseThrow(() -> new RuntimeException("No se puede modificar: La pista con ID " + id + " no existe."));

        if (!pistaExistente.getNombre().equalsIgnoreCase(pistaRequestDTO.getNombre()) && pistaRepository.existsByNombreIgnoreCase(pistaRequestDTO.getNombre())) {
            throw new RuntimeException("Ya existe otra pista con el nombre: " + pistaRequestDTO.getNombre());
        }

        pistaExistente.setNombre(pistaRequestDTO.getNombre());
        pistaExistente.setTipoDeporte(pistaRequestDTO.getTipoDeporte());
        pistaExistente.setMaxJugadores(pistaRequestDTO.getMaxJugadores());
        pistaExistente.setActiva(pistaRequestDTO.isActiva());

        Long nuevoPolideportivoId = Objects.requireNonNull(pistaRequestDTO.getPolideportivoId(), "polideportivoId no puede ser null");
        if (!pistaExistente.getPolideportivo().getId().equals(nuevoPolideportivoId)) {
        Polideportivo nuevoPoli = polideportivoRepository.findById(nuevoPolideportivoId)
            .orElseThrow(() -> new RuntimeException("El nuevo polideportivo con ID " + nuevoPolideportivoId + " no existe."));
        pistaExistente.setPolideportivo(nuevoPoli);
    }

        Pista pistaGuardada =  pistaRepository.save(pistaExistente);

        // pasamos a DTO para el controller
        PistaResponse pistaDto = convertirPistaToDto(pistaGuardada);

        return pistaDto;
    }


    @Transactional
    public void eliminarPista(Long id) {
        Objects.requireNonNull(id, "id no puede ser null");
        
        if (!pistaRepository.existsById(id)) {
            throw new RuntimeException("No se puede modificar: La pista con ID " + id + " no existe.");
        }

        try {
            pistaRepository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar la pista: Es posible que tenga reservas asociadas.");
        }
    }


    @Transactional
    public PistaResponse bloquearPista(Long id) {
        Objects.requireNonNull(id, "id no puede ser null");
        Pista pistaExistente = pistaRepository.findById(id).orElseThrow(() -> new RuntimeException("No se puede modificar: La pista con ID " + id + " no existe."));

        boolean antesEstabaActiva = pistaExistente.isActiva();
        pistaExistente.setActiva(!antesEstabaActiva);

        Pista pistaGuardada =  pistaRepository.save(pistaExistente);

        // Si la pista se acaba de BLOQUEAR (estaba activa y ahora no lo está)
        if (antesEstabaActiva && !pistaGuardada.isActiva()) {
            // Cancelar reservas desde hoy en adelante
            List<Reserva> reservasFuturas = reservaRepository.findActivasByPistaAndRango(
                    id, LocalDate.now(), LocalDate.now().plusYears(1)); // Un rango razonable

            for (Reserva r : reservasFuturas) {
                reservaService.cancelarReservaPorBloqueo(r.getId());
            }
        }

        // pasamos a DTO para el controller
        PistaResponse pistaDto = convertirPistaToDto(pistaGuardada);

        return pistaDto;
    }
}
