package com.deustosport.my_app.dto;

public class SecretariaReservaResumenDto {

    private Long id;
    private String fecha;
    private String horaInicio;
    private String horaFin;
    private String estado;
    private String pistaNombre;
    private Long pistaId;
    private String usuarioNombre;

    public SecretariaReservaResumenDto() {
    }

    public SecretariaReservaResumenDto(Long id, String fecha, String horaInicio, String horaFin,
            String estado, String pistaNombre, Long pistaId, String usuarioNombre) {
        this.id = id;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.estado = estado;
        this.pistaNombre = pistaNombre;
        this.pistaId = pistaId;
        this.usuarioNombre = usuarioNombre;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(String horaInicio) {
        this.horaInicio = horaInicio;
    }

    public String getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(String horaFin) {
        this.horaFin = horaFin;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getPistaNombre() {
        return pistaNombre;
    }

    public void setPistaNombre(String pistaNombre) {
        this.pistaNombre = pistaNombre;
    }

    public Long getPistaId() {
        return pistaId;
    }

    public void setPistaId(Long pistaId) {
        this.pistaId = pistaId;
    }

    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
    }
}
