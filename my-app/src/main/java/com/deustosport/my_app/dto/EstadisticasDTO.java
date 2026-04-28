package com.deustosport.my_app.dto;

import java.math.BigDecimal;

public class EstadisticasDTO {

    private long totalReservasConfirmadas;
    private long totalReservasPendientes;
    private long totalReservasCanceladas;
    private long reservasMesActual;
    private BigDecimal ingresosMesActual = BigDecimal.ZERO;
    private long reservasHoy;
    private BigDecimal ingresosHoy = BigDecimal.ZERO;
    private long totalSocios;
    private long totalNoSocios;
    private BigDecimal saldoTotalBilleteras = BigDecimal.ZERO;

    public EstadisticasDTO() {
    }

    public long getTotalReservasConfirmadas() {
        return totalReservasConfirmadas;
    }

    public void setTotalReservasConfirmadas(long totalReservasConfirmadas) {
        this.totalReservasConfirmadas = totalReservasConfirmadas;
    }

    public long getTotalReservasPendientes() {
        return totalReservasPendientes;
    }

    public void setTotalReservasPendientes(long totalReservasPendientes) {
        this.totalReservasPendientes = totalReservasPendientes;
    }

    public long getTotalReservasCanceladas() {
        return totalReservasCanceladas;
    }

    public void setTotalReservasCanceladas(long totalReservasCanceladas) {
        this.totalReservasCanceladas = totalReservasCanceladas;
    }

    public long getReservasMesActual() {
        return reservasMesActual;
    }

    public void setReservasMesActual(long reservasMesActual) {
        this.reservasMesActual = reservasMesActual;
    }

    public BigDecimal getIngresosMesActual() {
        return ingresosMesActual;
    }

    public void setIngresosMesActual(BigDecimal ingresosMesActual) {
        this.ingresosMesActual = ingresosMesActual;
    }

    public long getReservasHoy() {
        return reservasHoy;
    }

    public void setReservasHoy(long reservasHoy) {
        this.reservasHoy = reservasHoy;
    }

    public BigDecimal getIngresosHoy() {
        return ingresosHoy;
    }

    public void setIngresosHoy(BigDecimal ingresosHoy) {
        this.ingresosHoy = ingresosHoy;
    }

    public long getTotalSocios() {
        return totalSocios;
    }

    public void setTotalSocios(long totalSocios) {
        this.totalSocios = totalSocios;
    }

    public long getTotalNoSocios() {
        return totalNoSocios;
    }

    public void setTotalNoSocios(long totalNoSocios) {
        this.totalNoSocios = totalNoSocios;
    }

    public BigDecimal getSaldoTotalBilleteras() {
        return saldoTotalBilleteras;
    }

    public void setSaldoTotalBilleteras(BigDecimal saldoTotalBilleteras) {
        this.saldoTotalBilleteras = saldoTotalBilleteras;
    }
}
