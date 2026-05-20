package com.deustosport.my_app.service;

import com.deustosport.my_app.entity.AbonoUsuario;
import com.deustosport.my_app.entity.PlanAbono;
import com.deustosport.my_app.entity.Usuario;
import com.deustosport.my_app.enums.DuracionAbonos;
import com.deustosport.my_app.repository.AbonoUsuarioRepository;
import com.deustosport.my_app.repository.PlanAbonoRepository;
import com.deustosport.my_app.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class AbonoUsuarioService {

    @Autowired
    private AbonoUsuarioRepository abonoUsuarioRepository;

    @Autowired
    private PlanAbonoRepository planAbonoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private NotificacionService notificacionService;

    public List<PlanAbono> obtenerPlanesActivos() {
        // En un caso real usaríamos planAbonoRepository.findByActivoTrue()
        // Lo filtramos por stream para no obligarte a crear el método en el repo
        return planAbonoRepository.findAll().stream()
                .filter(PlanAbono::isActivo)
                .toList();
    }

    public PlanAbono obtenerPlanAdecuado(Long usuarioId, Long planId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        validarEdadUsuario(usuario, planId);

        return planAbonoRepository.findById(planId).get();
    }

    private void validarEdadUsuario(Usuario u, Long planId) {
        PlanAbono plan = planAbonoRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan no encontrado"));

        if (!plan.isActivo()) throw new RuntimeException("Este plan no se encuentra activo.");

        if (u.getFechaNacimiento() == null) {
            throw new RuntimeException("El usuario " + u.getEmail() + " debe tener fecha de nacimiento configurada.");
        }

        int edad = Period.between(u.getFechaNacimiento(), LocalDate.now()).getYears();
        if (edad < plan.getEdadMinima() || edad > plan.getEdadMax()) {
            throw new RuntimeException("El usuario " + u.getEmail() + " no cumple con los requisitos de edad (Edad permitida: " + plan.getEdadMinima() + "-" + plan.getEdadMax() + ").");
        }
    }

    @Transactional
    public AbonoUsuario comprarAbono(Long titularId, Long planId, List<String> emailsBeneficiarios, String metodoPago) {
        Usuario titular = usuarioRepository.findById(titularId)
                .orElseThrow(() -> new RuntimeException("Titular no encontrado"));

        if (obtenerAbonoActivo(titularId).isPresent()) {
            throw new RuntimeException("Ya tienes un abono activo en tu cuenta.");
        }

        PlanAbono plan = obtenerPlanAdecuado(titularId, planId);

        // Crear la suscripción
        AbonoUsuario nuevoAbono = new AbonoUsuario();
        nuevoAbono.setTitular(titular);
        nuevoAbono.setPlan(plan);

        // Validar límite de personas (1 titular + X beneficiarios)
        int totalPersonas = 1 + (emailsBeneficiarios != null ? emailsBeneficiarios.size() : 0);
        if (totalPersonas > plan.getCantidadPersonas()) {
            throw new RuntimeException("Este plan permite un máximo de " + plan.getCantidadPersonas() + " personas en total.");
        }

        // Buscar y validar beneficiarios
        List<Usuario> beneficiarios = new ArrayList<>();
        if (emailsBeneficiarios != null && !emailsBeneficiarios.isEmpty()) {
            for (String email : emailsBeneficiarios) {
                if (email.trim().equalsIgnoreCase(titular.getEmail())) continue;

                Usuario b = usuarioRepository.findByEmail(email.trim())
                        .orElseThrow(() -> new RuntimeException("No existe ningún usuario registrado con el email: " + email));
                
                if (obtenerAbonoActivo(b.getId()).isPresent()) {
                    throw new RuntimeException("El usuario " + email + " ya está disfrutando de otro abono activo.");
                }

                validarEdadUsuario(b, planId);
                beneficiarios.add(b);
            }
        }

        // --- LÓGICA DE PAGO ---
        if ("BILLETERA".equalsIgnoreCase(metodoPago)) {
            if (titular.getBilletera().compareTo(plan.getPrecio()) < 0) {
                throw new RuntimeException("Saldo insuficiente en billetera. El plan cuesta " + plan.getPrecio() + "€.");
            }
            titular.setBilletera(titular.getBilletera().subtract(plan.getPrecio()));
            usuarioRepository.save(titular);
        } else {
            // Para otros métodos (TARJETA, BIZUM, etc.) simulamos validación exitosa
            // En un sistema real aquí se llamaría a la pasarela de pago
            log.info("Procesando pago de abono via {} para el usuario {}", metodoPago, titular.getEmail());
        }

        nuevoAbono.setBeneficiarios(beneficiarios);
        nuevoAbono.setFechaInicio(LocalDate.now());
        nuevoAbono.setActivo(true);

        if (plan.getDuracion() == DuracionAbonos.MENSUAL) {
            nuevoAbono.setFechaFin(LocalDate.now().plusMonths(1));
        } else if (plan.getDuracion() == DuracionAbonos.TRIMESTRAL) {
            nuevoAbono.setFechaFin(LocalDate.now().plusMonths(3));
        } else if (plan.getDuracion() == DuracionAbonos.ANUAL) {
            nuevoAbono.setFechaFin(LocalDate.now().plusYears(1));
        } else {
            nuevoAbono.setFechaFin(LocalDate.now().plusDays(1)); // Seguridad
        }

        AbonoUsuario guardado = abonoUsuarioRepository.save(nuevoAbono);

        // Notificar al titular
        notificacionService.notificarCompraAbono(titular, plan.getNombre(), guardado.getFechaFin().toString());

        return guardado;
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

    @Transactional
    public int notificarVencimientos() {
        LocalDate manana = LocalDate.now().plusDays(1);
        List<AbonoUsuario> vencenManana = abonoUsuarioRepository.findByActivoTrueAndFechaFin(manana);
        for (AbonoUsuario abono : vencenManana) {
            notificacionService.notificarVencimientoProximo(abono.getTitular(), abono.getPlan().getNombre());
        }
        return vencenManana.size();
    }
}