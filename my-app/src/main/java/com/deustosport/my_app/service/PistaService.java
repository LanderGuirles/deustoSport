package com.deustosport.my_app.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.deustosport.my_app.dto.PistaRequest;
import com.deustosport.my_app.dto.PistaResponse;
import com.deustosport.my_app.entity.Instalacion;
import com.deustosport.my_app.entity.Pista;
import com.deustosport.my_app.repository.InstalacionRepository;
import com.deustosport.my_app.repository.PistaRepository;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PistaService {

    private final PistaRepository pistaRepository;
    private final InstalacionRepository instalacionRepository;

    public PistaService(PistaRepository pistaRepository, InstalacionRepository instalacionRepository) {
        this.pistaRepository = pistaRepository;
        this.instalacionRepository = instalacionRepository;
    }

    @Transactional
    public List<Pista> obtenerPistasPorInstalacionId(Long instalacionId){
        if (!instalacionRepository.existsById(instalacionId)) {
            throw new RuntimeException("No se puede listar: La instalación con ID " + instalacionId + " no existe.");
        }
        return pistaRepository.findByInstalacionId(instalacionId);
    }


    @Transactional
    public List<PistaResponse> obtenerTodasLasPistas() {
        List<Pista> pistas = pistaRepository.findAll();

        return pistas.stream().map(pista -> {
            PistaResponse dto = new PistaResponse();
            dto.setId(pista.getId());
            dto.setNombre(pista.getNombre());
            dto.setTipoDeporte(pista.getTipoDeporte());
            dto.setMaxJugadores(pista.getMaxJugadores());
            dto.setActiva(pista.isActiva());

            if (pista.getInstalacion() != null) {
                dto.setInstalacionId(pista.getInstalacion().getId());
                dto.setInstalacionNombre(pista.getInstalacion().getNombre());
            }

            return dto;
        }).collect(Collectors.toList());
    }


    @Transactional
    public Pista registrarNuevaPista(Pista nuevaPista) {

        nuevaPista.setId(null); 

        // Comprobar que no haya una pista registrada con ese nombre
        if (pistaRepository.existsByNombreIgnoreCase(nuevaPista.getNombre())) {
            throw new RuntimeException("Ya existe una pista con ese nombre.");
        }

        // Comprobar que venga el ID de la instalación
        if (nuevaPista.getInstalacion() == null || nuevaPista.getInstalacion().getId() == null) {
            throw new RuntimeException("Error: La pista debe estar vinculada a una instalación válida.");
        }

        Long instalacionId = nuevaPista.getInstalacion().getId();
        
        // Buscamos la instalación real
        Instalacion instalacionAsociada = instalacionRepository.findById(instalacionId)
                .orElseThrow(() -> new RuntimeException("Error: No se encontró la instalación con ID: " + instalacionId));

        nuevaPista.setInstalacion(instalacionAsociada);
        nuevaPista.setActiva(true);

        return pistaRepository.save(nuevaPista);
    }


    @Transactional
    public PistaResponse actualizarPista(Long id, PistaRequest pistaRequestDTO) {
        Pista pistaExistente = pistaRepository.findById(id).orElseThrow(() -> new RuntimeException("No se puede modificar: La pista con ID " + id + " no existe."));

        if (!pistaExistente.getNombre().equalsIgnoreCase(pistaRequestDTO.getNombre()) && pistaRepository.existsByNombreIgnoreCase(pistaRequestDTO.getNombre())) {
            throw new RuntimeException("Ya existe otra pista con el nombre: " + pistaRequestDTO.getNombre());
        }

        pistaExistente.setNombre(pistaRequestDTO.getNombre());
        pistaExistente.setTipoDeporte(pistaRequestDTO.getTipoDeporte());
        pistaExistente.setMaxJugadores(pistaRequestDTO.getMaxJugadores());
        pistaExistente.setActiva(pistaRequestDTO.isActiva());

        if (!pistaExistente.getInstalacion().getId().equals(pistaRequestDTO.getInstalacionId())) {
        Instalacion nuevaInst = instalacionRepository.findById(pistaRequestDTO.getInstalacionId())
                .orElseThrow(() -> new RuntimeException("La nueva instalación con ID " + pistaRequestDTO .getInstalacionId() + " no existe."));
        pistaExistente.setInstalacion(nuevaInst);
    }

        Pista pistaGuardada =  pistaRepository.save(pistaExistente);

        // pasamos a DTO para el controller
        PistaResponse pistaResponseDto = new PistaResponse();
        pistaResponseDto.setId(pistaGuardada.getId());
        pistaResponseDto.setNombre(pistaGuardada.getNombre());
        pistaResponseDto.setTipoDeporte(pistaGuardada.getTipoDeporte());
        pistaResponseDto.setMaxJugadores(pistaGuardada.getMaxJugadores());
        pistaResponseDto.setActiva(pistaGuardada.isActiva());

        if (pistaGuardada.getInstalacion() != null) {
            pistaResponseDto.setInstalacionId(pistaGuardada.getInstalacion().getId());
            pistaResponseDto.setInstalacionNombre(pistaGuardada.getInstalacion().getNombre());
        }

        return pistaResponseDto;
    }


    @Transactional
    public void eliminarPista(Long id) {
        
        if (!pistaRepository.existsById(id)) {
            throw new RuntimeException("No se puede modificar: La pista con ID " + id + " no existe.");
        }

        try {
            pistaRepository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar la pista: Es posible que tenga reservas asociadas.");
        }
    }
}
