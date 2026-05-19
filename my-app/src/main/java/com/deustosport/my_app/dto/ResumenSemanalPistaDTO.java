package com.deustosport.my_app.dto;

import com.deustosport.my_app.enums.TipoDeporte;
import java.math.BigDecimal;

public class ResumenSemanalPistaDTO {

    private Long pistaId;
    private String pistaNombre;
    private TipoDeporte tipoDeporte;
    private boolean activa;
    private long totalReservasSemana;
    private long reservasConfirmadas;
    private long reservasCanceladas;
    private BigDecimal ingresosSemana;
    private long horasReservadas;
    private long horasDisponibles;
    private double tasaOcupacion;

    public ResumenSemanalPistaDTO() {}

    public Long getPistaId() { return pistaId; }
    public void setPistaId(Long pistaId) { this.pistaId = pistaId; }

    public String getPistaNombre() { return pistaNombre; }
    public void setPistaNombre(String pistaNombre) { this.pistaNombre = pistaNombre; }

    public TipoDeporte getTipoDeporte() { return tipoDeporte; }
    public void setTipoDeporte(TipoDeporte tipoDeporte) { this.tipoDeporte = tipoDeporte; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }

    public long getTotalReservasSemana() { return totalReservasSemana; }
    public void setTotalReservasSemana(long totalReservasSemana) { this.totalReservasSemana = totalReservasSemana; }

    public long getReservasConfirmadas() { return reservasConfirmadas; }
    public void setReservasConfirmadas(long reservasConfirmadas) { this.reservasConfirmadas = reservasConfirmadas; }

    public long getReservasCanceladas() { return reservasCanceladas; }
    public void setReservasCanceladas(long reservasCanceladas) { this.reservasCanceladas = reservasCanceladas; }

    public BigDecimal getIngresosSemana() { return ingresosSemana; }
    public void setIngresosSemana(BigDecimal ingresosSemana) { this.ingresosSemana = ingresosSemana; }

    public long getHorasReservadas() { return horasReservadas; }
    public void setHorasReservadas(long horasReservadas) { this.horasReservadas = horasReservadas; }

    public long getHorasDisponibles() { return horasDisponibles; }
    public void setHorasDisponibles(long horasDisponibles) { this.horasDisponibles = horasDisponibles; }

    public double getTasaOcupacion() { return tasaOcupacion; }
    public void setTasaOcupacion(double tasaOcupacion) { this.tasaOcupacion = tasaOcupacion; }
}
