package com.deustosport.my_app.service;

import com.deustosport.my_app.entity.Polideportivo;
import com.deustosport.my_app.entity.Pista;
import com.deustosport.my_app.repository.PolideportivoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

@Service
public class PolideportivoService {

    private final PolideportivoRepository polideportivoRepository;

    public PolideportivoService(PolideportivoRepository polideportivoRepository) {
        this.polideportivoRepository = polideportivoRepository;
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
        if (polideportivoRepository.findAll().stream().anyMatch(p -> 
            p.getNombre().equalsIgnoreCase(polideportivo.getNombre()) && 
            p.getDireccion().equalsIgnoreCase(polideportivo.getDireccion()))) {
            throw new IllegalArgumentException("Ya existe un polideportivo con el mismo nombre y dirección.");
        }
        return polideportivoRepository.save(polideportivo);
    }

    @Transactional
    public Polideportivo actualizarPolideportivo(Long id, String nombre, String direccion) {
        Polideportivo poli = polideportivoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Polideportivo no encontrado"));
        
        // Verificar duplicados excluyendo el actual
        if (polideportivoRepository.findAll().stream().anyMatch(p -> 
            !p.getId().equals(id) &&
            p.getNombre().equalsIgnoreCase(nombre) && 
            p.getDireccion().equalsIgnoreCase(direccion))) {
            throw new IllegalArgumentException("Ya existe otro polideportivo con el mismo nombre y dirección.");
        }

        poli.setNombre(nombre);
        poli.setDireccion(direccion);
        return polideportivoRepository.save(poli);
    }
}
