package com.deustosport.my_app.service;

import com.deustosport.my_app.entity.AbonoUsuario;
import com.deustosport.my_app.entity.TarifaAbono;
import com.deustosport.my_app.entity.Usuario;
import com.deustosport.my_app.enums.DuracionAbonos;
import com.deustosport.my_app.repository.AbonoUsuarioRepository;
import com.deustosport.my_app.repository.TarifaAbonoRepository;
import com.deustosport.my_app.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

@Service
public class AbonoUsuarioService {

    @Autowired
    private AbonoUsuarioRepository abonoUsuarioRepository;

    @Autowired
    private TarifaAbonoRepository tarifaAbonoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public TarifaAbono calcularTarifaParaUsuario(Long usuarioId, Long planId, DuracionAbonos duracion) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuario.getFechaNacimiento() == null) {
            throw new RuntimeException("El usuario debe tener fecha de nacimiento configurada para ver tarifas.");
        }

        int edad = Period.between(usuario.getFechaNacimiento(), LocalDate.now()).getYears();

        return tarifaAbonoRepository.findTarifaExactaParaUsuario(planId, duracion, edad)
                .orElseThrow(() -> new RuntimeException("No hay tarifas disponibles para tu edad en este plan."));
    }

    @Transactional
    public AbonoUsuario comprarAbono(Long usuarioId, Long tarifaId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Optional<AbonoUsuario> abonoActivo = obtenerAbonoActivo(usuarioId);
        if (abonoActivo.isPresent()) {
            throw new RuntimeException("El usuario ya tiene un abono activo hasta " + abonoActivo.get().getFechaFin());
        }

        TarifaAbono tarifa = tarifaAbonoRepository.findById(tarifaId)
                .orElseThrow(() -> new RuntimeException("Tarifa no encontrada"));


        if (usuario.getBilletera().compareTo(tarifa.getPrecio()) < 0) {
            throw new RuntimeException("Saldo insuficiente. La tarifa cuesta " + tarifa.getPrecio() + "€ y tienes " + usuario.getBilletera() + "€.");
        }
        
        usuario.setBilletera(usuario.getBilletera().subtract(tarifa.getPrecio()));
        usuarioRepository.save(usuario);

        // Crear el registro de compra
        AbonoUsuario nuevoAbono = new AbonoUsuario();
        nuevoAbono.setUsuario(usuario);
        nuevoAbono.setTarifa(tarifa);
        nuevoAbono.setFechaInicio(LocalDate.now());
        nuevoAbono.setActivo(true);

        if (tarifa.getDuracion() == DuracionAbonos.MENSUAL) {
            nuevoAbono.setFechaFin(LocalDate.now().plusMonths(1));
        } else if (tarifa.getDuracion() == DuracionAbonos.ANUAL) {
            nuevoAbono.setFechaFin(LocalDate.now().plusYears(1));
        } else {
            nuevoAbono.setFechaFin(LocalDate.now().plusDays(1));
        }

        return abonoUsuarioRepository.save(nuevoAbono);
    }

    public Optional<AbonoUsuario> obtenerAbonoActivo(Long usuarioId) {
        return abonoUsuarioRepository.findAbonoActivoByUsuarioId(usuarioId, LocalDate.now());
    }

    @Transactional
    public int desactivarAbonosCaducados() {
        List<AbonoUsuario> caducados = abonoUsuarioRepository.findByActivoTrueAndFechaFinBefore(LocalDate.now());
        for (AbonoUsuario abono : caducados) {
            abono.setActivo(false);
        }
        abonoUsuarioRepository.saveAll(caducados);
        return caducados.size();
    }
}