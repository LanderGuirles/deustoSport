package com.deustosport.my_app.service;

import com.deustosport.my_app.entity.Instalacion;
import com.deustosport.my_app.repository.InstalacionRepository;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InstalacionService {

    private final InstalacionRepository instalacionRepository;

    public InstalacionService(InstalacionRepository instalacionRepository) {
        this.instalacionRepository = instalacionRepository;
    }

    @Transactional(readOnly = true)
    public boolean existeInstalacion(Long id) {
        Objects.requireNonNull(id, "id no puede ser null");
        return instalacionRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public List<Instalacion> obtenerTodas() {
        return instalacionRepository.findAll();
    }

    @Transactional
    public Instalacion actualizarHorarioGeneral(Long instalacionId, LocalTime horaApertura, LocalTime horaCierre) {
        Objects.requireNonNull(instalacionId, "instalacionId no puede ser null");
        if (horaApertura == null || horaCierre == null) {
            throw new IllegalArgumentException("La hora de apertura y cierre son obligatorias.");
        }

        if (!horaCierre.isAfter(horaApertura)) {
            throw new IllegalArgumentException("La hora de cierre debe ser posterior a la hora de apertura.");
        }

        Instalacion instalacion = instalacionRepository.findById(instalacionId)
                .orElseThrow(() -> new IllegalArgumentException("Instalación no encontrada con ID: " + instalacionId));

        instalacion.setHoraApertura(horaApertura);
        instalacion.setHoraCierre(horaCierre);
        return instalacionRepository.save(instalacion);
    }
}