package com.deustosport.my_app.service;

import com.deustosport.my_app.entity.Bono;
import com.deustosport.my_app.repository.BonoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BonoService {

    @Autowired
    private BonoRepository bonoRepository;

    @Transactional(readOnly = true)
    public List<Bono> obtenerBonosActivos() {
        return bonoRepository.findByActivoTrue();
    }

    @Transactional(readOnly = true)
    public List<Bono> obtenerTodos() {
        return bonoRepository.findAll();
    }
}
