package com.deustosport.my_app.service;

import com.deustosport.my_app.entity.AbonoUsuario;
import com.deustosport.my_app.entity.PlanAbono;
import com.deustosport.my_app.entity.Usuario;
import com.deustosport.my_app.enums.DuracionAbonos;
import com.deustosport.my_app.repository.AbonoUsuarioRepository;
import com.deustosport.my_app.repository.PlanAbonoRepository;
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
    private PlanAbonoRepository planAbonoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public PlanAbono obtenerPlanAdecuado(Long usuarioId, Long planId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuario.getFechaNacimiento() == null) {
            throw new RuntimeException("El usuario debe tener fecha de nacimiento configurada para ver planes.");
        }

        int edad = Period.between(usuario.getFechaNacimiento(), LocalDate.now()).getYears();

        PlanAbono plan = planAbonoRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan no encontrado"));

        if (!plan.isActivo()) {
            throw new RuntimeException("Este plan no se encuentra activo.");
        }

        if (edad < plan.getEdadMinima() || edad > plan.getEdadMax()) {
            throw new RuntimeException("No cumples con los requisitos de edad para este plan.");
        }

        return plan;
    }

    @Transactional
    public AbonoUsuario comprarAbono(Long usuarioId, Long planId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Optional<AbonoUsuario> abonoActivo = obtenerAbonoActivo(usuarioId);
        if (abonoActivo.isPresent()) {
            throw new RuntimeException("El usuario ya tiene un abono activo hasta " + abonoActivo.get().getFechaFin());
        }

        PlanAbono plan = obtenerPlanAdecuado(usuarioId, planId);

        if (usuario.getBilletera().compareTo(plan.getPrecio()) < 0) {
            throw new RuntimeException("Saldo insuficiente. El plan cuesta " + plan.getPrecio() + "€ y tienes " + usuario.getBilletera() + "€.");
        }
        
        usuario.setBilletera(usuario.getBilletera().subtract(plan.getPrecio()));
        usuarioRepository.save(usuario);

        AbonoUsuario nuevoAbono = new AbonoUsuario();
        nuevoAbono.setUsuario(usuario);
        nuevoAbono.setPlan(plan);
        nuevoAbono.setFechaInicio(LocalDate.now());
        nuevoAbono.setActivo(true);

        if (plan.getDuracion() == DuracionAbonos.MENSUAL) {
            nuevoAbono.setFechaFin(LocalDate.now().plusMonths(1));
        } else if (plan.getDuracion() == DuracionAbonos.TRIMESTRAL) {
            nuevoAbono.setFechaFin(LocalDate.now().plusMonths(3));
        } else if (plan.getDuracion() == DuracionAbonos.ANUAL) {
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