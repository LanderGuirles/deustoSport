package com.deustosport.my_app.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class RecargarBilleteraRequest {
    @NotNull(message = "Cantidad es obligatoria")
    @DecimalMin(value = "0.01", message = "Cantidad debe ser mayor a 0")
    private BigDecimal cantidad;

    // Getters and setters
    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }
}
