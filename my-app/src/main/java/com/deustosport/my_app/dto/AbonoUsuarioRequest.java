package com.deustosport.my_app.dto;

import lombok.Data;
import java.util.List;

@Data
public class AbonoUsuarioRequest {
    private Long usuarioId;
    private Long planAbonoId;
    private List<String> emailsBeneficiarios;
    private String metodoPago;
    private String ambito; // LOCAL o CIUDAD
    private Long polideportivoId; // Solo si ambito == LOCAL
    private Long ayuntamientoId; // Solo si ambito == CIUDAD
}