package com.deustosport.my_app.service;

import com.deustosport.my_app.entity.PlanAbono;
import com.deustosport.my_app.entity.TarifaAbono;
import com.deustosport.my_app.repository.PlanAbonoRepository;
import com.deustosport.my_app.repository.TarifaAbonoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class AbonoConfigService {

    @Autowired
    private PlanAbonoRepository planAbonoRepository;

    @Autowired
    private TarifaAbonoRepository tarifaAbonoRepository;

    @Transactional
    public PlanAbono crearPlan(PlanAbono plan) {
        return planAbonoRepository.save(plan);
    }

    public List<PlanAbono> listarPlanes() {
        return planAbonoRepository.findAll();
    }

    @Transactional
    public PlanAbono actualizarPlan(Long id, PlanAbono detalles) {
        PlanAbono plan = planAbonoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Plan no encontrado"));
        plan.setNombre(detalles.getNombre());
        plan.setDescuentoPistasPorcentaje(detalles.getDescuentoPistasPorcentaje());
        return planAbonoRepository.save(plan);
    }

    @Transactional
    public void eliminarPlan(Long id) {
        planAbonoRepository.deleteById(id);
    }

    @Transactional
    public TarifaAbono añadirTarifaAPlan(Long planId, TarifaAbono tarifa) {
        PlanAbono plan = planAbonoRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan no encontrado"));
        
        List<TarifaAbono> existentes = tarifaAbonoRepository.findByPlanAbonoId(planId);
        for (TarifaAbono existente : existentes) {
            if (existente.getDuracion() == tarifa.getDuracion()) {
                if (tarifa.getEdadMinima() <= existente.getEdadMax() && tarifa.getEdadMax() >= existente.getEdadMinima()) {
                    throw new IllegalArgumentException("Solapamiento de edades. Ya existe una tarifa para edades " 
                        + existente.getEdadMinima() + "-" + existente.getEdadMax());
                }
            }
        }

        tarifa.setPlanAbono(plan);
        return tarifaAbonoRepository.save(tarifa);
    }

    public List<TarifaAbono> listarTarifasDePlan(Long planId) {
        return tarifaAbonoRepository.findByPlanAbonoId(planId);
    }

    @Transactional
    public TarifaAbono actualizarTarifa(Long id, TarifaAbono detalles) {
        TarifaAbono tarifa = tarifaAbonoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Tarifa no encontrada"));
        tarifa.setPrecio(detalles.getPrecio());
        return tarifaAbonoRepository.save(tarifa);
    }

    @Transactional
    public void eliminarTarifa(Long id) {
        tarifaAbonoRepository.deleteById(id);
    }
}