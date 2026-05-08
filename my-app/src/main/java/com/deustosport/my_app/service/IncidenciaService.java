package com.deustosport.my_app.service;

import com.deustosport.my_app.dto.IncidenciaResponse;
import com.deustosport.my_app.dto.RegistrarIncidenciaRequest;
import com.deustosport.my_app.entity.Incidencia;
import com.deustosport.my_app.entity.Instalacion;
import com.deustosport.my_app.entity.Pista;
import com.deustosport.my_app.entity.Usuario;
import com.deustosport.my_app.enums.EstadoIncidencia;
import com.deustosport.my_app.repository.IncidenciaRepository;
import com.deustosport.my_app.repository.InstalacionRepository;
import com.deustosport.my_app.repository.PistaRepository;
import com.deustosport.my_app.repository.UsuarioRepository;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IncidenciaService {

    private final IncidenciaRepository incidenciaRepository;
    private final UsuarioRepository usuarioRepository;
    private final InstalacionRepository instalacionRepository;
    private final PistaRepository pistaRepository;
    private final NotificacionService notificacionService;

    public IncidenciaService(IncidenciaRepository incidenciaRepository,
                             UsuarioRepository usuarioRepository,
                             InstalacionRepository instalacionRepository,
                             PistaRepository pistaRepository,
                             NotificacionService notificacionService) {
        this.incidenciaRepository = incidenciaRepository;
        this.usuarioRepository = usuarioRepository;
        this.instalacionRepository = instalacionRepository;
        this.pistaRepository = pistaRepository;
        this.notificacionService = notificacionService;
    }

    @Transactional
    public IncidenciaResponse registrarIncidencia(RegistrarIncidenciaRequest request) {
        Objects.requireNonNull(request, "request no puede ser null");

        if (request.getUsuarioId() == null) {
            throw new IllegalArgumentException("El usuario que reporta es obligatorio.");
        }
        if (request.getInstalacionId() == null) {
            throw new IllegalArgumentException("La instalación es obligatoria.");
        }
        if (request.getTitulo() == null || request.getTitulo().isBlank()) {
            throw new IllegalArgumentException("El título de la incidencia es obligatorio.");
        }
        if (request.getDescripcion() == null || request.getDescripcion().isBlank()) {
            throw new IllegalArgumentException("La descripción de la incidencia es obligatoria.");
        }

        Usuario reportadaPor = usuarioRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        Instalacion instalacion = instalacionRepository.findById(request.getInstalacionId())
                .orElseThrow(() -> new IllegalArgumentException("Instalación no encontrada"));

        Pista pista = null;
        if (request.getPistaId() != null) {
            pista = pistaRepository.findById(request.getPistaId())
                    .orElseThrow(() -> new IllegalArgumentException("Pista no encontrada"));
            if (!pista.getInstalacion().getId().equals(instalacion.getId())) {
                throw new IllegalArgumentException("La pista no pertenece a la instalación seleccionada.");
            }
        }

        Incidencia incidencia = new Incidencia();
        incidencia.setReportadaPor(reportadaPor);
        incidencia.setInstalacion(instalacion);
        incidencia.setPista(pista);
        incidencia.setTitulo(request.getTitulo().trim());
        incidencia.setDescripcion(request.getDescripcion().trim());
        incidencia.setUbicacionEspecifica(
                request.getUbicacionEspecifica() != null && !request.getUbicacionEspecifica().isBlank()
                        ? request.getUbicacionEspecifica().trim()
                        : null);
        incidencia.setEstado(EstadoIncidencia.ABIERTA);

        Incidencia guardada = incidenciaRepository.save(incidencia);
        notificacionService.alertarCoordinacionSobreIncidencia(guardada);

        return toDto(guardada);
    }

    @Transactional(readOnly = true)
    public List<IncidenciaResponse> listarIncidencias(EstadoIncidencia estado) {
        List<Incidencia> incidencias = estado == null
                ? incidenciaRepository.findAllByOrderByFechaCreacionDesc()
                : incidenciaRepository.findByEstadoOrderByFechaCreacionDesc(estado);

        return incidencias.stream().map(this::toDto).collect(Collectors.toList());
    }

    private IncidenciaResponse toDto(Incidencia incidencia) {
        IncidenciaResponse response = new IncidenciaResponse();
        response.setId(incidencia.getId());
        response.setReportadaPorId(incidencia.getReportadaPor().getId());
        response.setReportadaPorNombre(incidencia.getReportadaPor().getNombreCompleto());
        response.setInstalacionId(incidencia.getInstalacion().getId());
        response.setInstalacionNombre(incidencia.getInstalacion().getNombre());
        response.setPistaId(incidencia.getPista() != null ? incidencia.getPista().getId() : null);
        response.setPistaNombre(incidencia.getPista() != null ? incidencia.getPista().getNombre() : null);
        response.setTitulo(incidencia.getTitulo());
        response.setDescripcion(incidencia.getDescripcion());
        response.setUbicacionEspecifica(incidencia.getUbicacionEspecifica());
        response.setEstado(incidencia.getEstado());
        response.setFechaCreacion(incidencia.getFechaCreacion());
        response.setFechaActualizacion(incidencia.getFechaActualizacion());
        return response;
    }
}