package com.deustosport.my_app.dto;

import java.util.List;
import lombok.Data;

@Data
public class EnviarComunicadoRequest {
    private String titulo;
    private String mensaje;
    private List<Long> destinatarios;
    private boolean enviarEmail;
}
