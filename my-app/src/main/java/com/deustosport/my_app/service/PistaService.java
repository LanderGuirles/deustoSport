package com.deustosport.my_app.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.deustosport.my_app.entity.Instalacion;
import com.deustosport.my_app.entity.Pista;
import com.deustosport.my_app.repository.InstalacionRepository;
import com.deustosport.my_app.repository.PistaRepository;
import java.util.List;

@Service
public class PistaService {

    private final PistaRepository pistaRepository;
    private final InstalacionRepository instalacionRepository;

    public PistaService(PistaRepository pistaRepository, InstalacionRepository instalacionRepository) {
        this.pistaRepository = pistaRepository;
        this.instalacionRepository = instalacionRepository;
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

    /**
     * Obtiene todas las pistas activas.
     */
    public List<Pista> obtenerTodasLasPistas() {
        return pistaRepository.findByActivaTrue();
    }
}
