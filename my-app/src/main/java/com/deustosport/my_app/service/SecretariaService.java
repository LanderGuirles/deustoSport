package com.deustosport.my_app.service;

import com.deustosport.my_app.dto.SecretariaReservaResumenDto;
import com.deustosport.my_app.dto.SecretariaUsuarioResumenDto;
import com.deustosport.my_app.entity.Reserva;
import com.deustosport.my_app.entity.Usuario;
import com.deustosport.my_app.repository.ReservaRepository;
import com.deustosport.my_app.repository.UsuarioRepository;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SecretariaService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final UsuarioRepository usuarioRepository;
    private final ReservaRepository reservaRepository;

    public SecretariaService(UsuarioRepository usuarioRepository, ReservaRepository reservaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.reservaRepository = reservaRepository;
    }

    @Transactional(readOnly = true)
    public List<SecretariaUsuarioResumenDto> buscarUsuarios(String dni) {
        String dniFiltro = normalizarFiltro(dni);

        List<Usuario> usuarios = usuarioRepository.buscarParaSecretaria(dniFiltro);

        return usuarios.stream()
                .map(usuario -> new SecretariaUsuarioResumenDto(
                        usuario.getId(),
                        usuario.getNombreCompleto(),
                        usuario.getDni(),
                        usuario.getEmail(),
                        usuario.getTelefono(),
                        usuario.isActivo(),
                        reservaRepository.countByUsuarioId(usuario.getId())
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SecretariaReservaResumenDto> obtenerReservasPorUsuario(Long usuarioId) {
        List<Reserva> reservas = reservaRepository.findByUsuarioIdOrderByFechaReservaDescHoraInicioDesc(usuarioId);

        return reservas.stream()
                .map(reserva -> new SecretariaReservaResumenDto(
                        reserva.getId(),
                        String.valueOf(reserva.getFechaReserva()),
                        reserva.getHoraInicio().format(TIME_FORMATTER),
                        reserva.getHoraFin().format(TIME_FORMATTER),
                        reserva.getEstado().name(),
                        reserva.getPista() != null ? reserva.getPista().getNombre() : "-"
                ))
                .toList();
    }

    private String normalizarFiltro(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim();
        return limpio.isEmpty() ? null : limpio;
    }
}
