package com.deustosport.my_app.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FestivoRequest {
    private String nombre;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
}
