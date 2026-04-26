package com.deustosport.my_app.dto;

import lombok.Data;

@Data
public class AbonoUsuarioRequest {
    private Long usuarioId;
    private Long tarifaAbonoId;
}
