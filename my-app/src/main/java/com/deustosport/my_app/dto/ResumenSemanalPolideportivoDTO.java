package com.deustosport.my_app.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ResumenSemanalPolideportivoDTO {

    private Long polideportivoId;
    private String polideportivoNombre;
    private String direccion;
    private LocalDate inicioSemana;
    private LocalDate finSemana;
    private int totalPistas;
    private int pistasActivas;
    private long totalReservasSemana;
    private long reservasConfirmadasSemana;
    private long reservasCanceladasSemana;
    private BigDecimal ingresosTotalesSemana;
    private List<ResumenSemanalPistaDTO> detallesPorPista;

    public ResumenSemanalPolideportivoDTO() {}

    public Long getPolideportivoId() { return polideportivoId; }
    public void setPolideportivoId(Long polideportivoId) { this.polideportivoId = polideportivoId; }

    public String getPolideportivoNombre() { return polideportivoNombre; }
    public void setPolideportivoNombre(String polideportivoNombre) { this.polideportivoNombre = polideportivoNombre; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public LocalDate getInicioSemana() { return inicioSemana; }
    public void setInicioSemana(LocalDate inicioSemana) { this.inicioSemana = inicioSemana; }

    public LocalDate getFinSemana() { return finSemana; }
    public void setFinSemana(LocalDate finSemana) { this.finSemana = finSemana; }

    public int getTotalPistas() { return totalPistas; }
    public void setTotalPistas(int totalPistas) { this.totalPistas = totalPistas; }

    public int getPistasActivas() { return pistasActivas; }
    public void setPistasActivas(int pistasActivas) { this.pistasActivas = pistasActivas; }

    public long getTotalReservasSemana() { return totalReservasSemana; }
    public void setTotalReservasSemana(long totalReservasSemana) { this.totalReservasSemana = totalReservasSemana; }

    public long getReservasConfirmadasSemana() { return reservasConfirmadasSemana; }
    public void setReservasConfirmadasSemana(long reservasConfirmadasSemana) { this.reservasConfirmadasSemana = reservasConfirmadasSemana; }

    public long getReservasCanceladasSemana() { return reservasCanceladasSemana; }
    public void setReservasCanceladasSemana(long reservasCanceladasSemana) { this.reservasCanceladasSemana = reservasCanceladasSemana; }

    public BigDecimal getIngresosTotalesSemana() { return ingresosTotalesSemana; }
    public void setIngresosTotalesSemana(BigDecimal ingresosTotalesSemana) { this.ingresosTotalesSemana = ingresosTotalesSemana; }

    public List<ResumenSemanalPistaDTO> getDetallesPorPista() { return detallesPorPista; }
    public void setDetallesPorPista(List<ResumenSemanalPistaDTO> detallesPorPista) { this.detallesPorPista = detallesPorPista; }
}
