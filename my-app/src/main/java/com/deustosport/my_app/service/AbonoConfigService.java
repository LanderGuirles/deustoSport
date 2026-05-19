package com.deustosport.my_app.service;

import com.deustosport.my_app.entity.PlanAbono;
import com.deustosport.my_app.repository.PlanAbonoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class AbonoConfigService {

    @Autowired
    private PlanAbonoRepository planAbonoRepository;

    @Transactional
    public PlanAbono crearPlan(PlanAbono plan) {
        return planAbonoRepository.save(plan);
    }

    @Transactional(readOnly = true)
    public List<PlanAbono> listarPlanes() {
        return planAbonoRepository.findAll();
    }

    @Transactional
    public PlanAbono actualizarPlan(Long id, PlanAbono detalles) {
        PlanAbono plan = planAbonoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Plan no encontrado"));
            
        plan.setNombre(detalles.getNombre());
        plan.setDescripcion(detalles.getDescripcion());
        plan.setCantidadPersonas(detalles.getCantidadPersonas());
        plan.setEdadMinima(detalles.getEdadMinima());
        plan.setEdadMax(detalles.getEdadMax());
        plan.setPrecio(detalles.getPrecio());
        plan.setDuracion(detalles.getDuracion());
        plan.setDescuentoPistasPorcentaje(detalles.getDescuentoPistasPorcentaje());
        plan.setActivo(detalles.isActivo());
        
        return planAbonoRepository.save(plan);
    }

    @Transactional(readOnly = true)
    public List<PlanAbono> listarPlanesPorAyuntamiento(Long ayuntamientoId) {
        return planAbonoRepository.findAll().stream()
                .filter(p -> p.getAyuntamiento() != null && p.getAyuntamiento().getId().equals(ayuntamientoId))
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional
    public void eliminarPlan(Long id) {
        planAbonoRepository.deleteById(id);
    }
}