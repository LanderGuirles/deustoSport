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

    @Transactional(readOnly = true)
    public List<Instalacion> obtenerPorPolideportivo(Long polideportivoId) {
        // Asumiendo que añadiremos este método al repositorio
        return instalacionRepository.findByPolideportivoId(polideportivoId);
    }
}