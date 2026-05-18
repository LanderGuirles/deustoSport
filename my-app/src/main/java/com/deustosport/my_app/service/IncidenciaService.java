package com.deustosport.my_app.service;

import com.deustosport.my_app.dto.IncidenciaResponse;
import com.deustosport.my_app.dto.RegistrarIncidenciaRequest;
import com.deustosport.my_app.entity.Incidencia;
import com.deustosport.my_app.entity.Polideportivo;
import com.deustosport.my_app.entity.Pista;
import com.deustosport.my_app.entity.Usuario;
import com.deustosport.my_app.enums.EstadoIncidencia;
import com.deustosport.my_app.repository.IncidenciaRepository;
import com.deustosport.my_app.repository.PolideportivoRepository;
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
    private final PolideportivoRepository polideportivoRepository;
    private final PistaRepository pistaRepository;
    private final NotificacionService notificacionService;

    public IncidenciaService(IncidenciaRepository incidenciaRepository,
                             UsuarioRepository usuarioRepository,
                             PolideportivoRepository polideportivoRepository,
                             PistaRepository pistaRepository,
                             NotificacionService notificacionService) {
        this.incidenciaRepository = incidenciaRepository;
        this.usuarioRepository = usuarioRepository;
        this.polideportivoRepository = polideportivoRepository;
        this.pistaRepository = pistaRepository;
        this.notificacionService = notificacionService;
    }

    @Transactional
    public IncidenciaResponse registrarIncidencia(RegistrarIncidenciaRequest request) {
        Objects.requireNonNull(request, "request no puede ser null");

        if (request.getUsuarioId() == null) {
            throw new IllegalArgumentException("El usuario que reporta es obligatorio.");
        }
        if (request.getPolideportivoId() == null) {
            throw new IllegalArgumentException("El polideportivo es obligatorio.");
        }
        if (request.getTitulo() == null || request.getTitulo().isBlank()) {
            throw new IllegalArgumentException("El título de la incidencia es obligatorio.");
        }
        if (request.getDescripcion() == null || request.getDescripcion().isBlank()) {
            throw new IllegalArgumentException("La descripción de la incidencia es obligatoria.");
        }

        Usuario reportadaPor = usuarioRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        Polideportivo polideportivo = polideportivoRepository.findById(request.getPolideportivoId())
                .orElseThrow(() -> new IllegalArgumentException("Polideportivo no encontrado"));

        Pista pista = null;
        if (request.getPistaId() != null) {
            pista = pistaRepository.findById(request.getPistaId())
                    .orElseThrow(() -> new IllegalArgumentException("Pista no encontrada"));
            if (!pista.getPolideportivo().getId().equals(polideportivo.getId())) {
                throw new IllegalArgumentException("La pista no pertenece al polideportivo seleccionado.");
            }
        }

        Incidencia incidencia = new Incidencia();
        incidencia.setReportadaPor(reportadaPor);
        incidencia.setPolideportivo(polideportivo);
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
        response.setPolideportivoId(incidencia.getPolideportivo().getId());
        response.setPolideportivoNombre(incidencia.getPolideportivo().getNombre());
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