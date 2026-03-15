package com.deustosport.my_app.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class ReservaRequest {
    private Long usuarioId;
    private Long pistaId;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private Integer duracionMinutos;

    // Constructor vacío
    public ReservaRequest() {
    }

    // Constructor con campos
    public ReservaRequest(Long usuarioId, Long pistaId, LocalDate fecha, LocalTime horaInicio, Integer duracionMinutos) {
        this.usuarioId = usuarioId;
        this.pistaId = pistaId;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.duracionMinutos = duracionMinutos;
    }

    // Getters y Setters
    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getPistaId() {
        return pistaId;
    }

    public void setPistaId(Long pistaId) {
        this.pistaId = pistaId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public Integer getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(Integer duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }
}