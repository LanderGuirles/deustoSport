package com.deustosport.my_app.dto;

import com.deustosport.my_app.enums.TipoDeporte;

public class PistaDisponibleDTO {

    private Long pistaId;
    private String pistaNombre;
    private TipoDeporte tipoDeporte;
    private int maxJugadores;
    private Long polideportivoId;
    private String polideportivoNombre;
    private String polideportivoDireccion;
    private String horaApertura;
    private String horaCierre;

    public PistaDisponibleDTO() {}

    public Long getPistaId() { return pistaId; }
    public void setPistaId(Long pistaId) { this.pistaId = pistaId; }

    public String getPistaNombre() { return pistaNombre; }
    public void setPistaNombre(String pistaNombre) { this.pistaNombre = pistaNombre; }

    public TipoDeporte getTipoDeporte() { return tipoDeporte; }
    public void setTipoDeporte(TipoDeporte tipoDeporte) { this.tipoDeporte = tipoDeporte; }

    public int getMaxJugadores() { return maxJugadores; }
    public void setMaxJugadores(int maxJugadores) { this.maxJugadores = maxJugadores; }

    public Long getPolideportivoId() { return polideportivoId; }
    public void setPolideportivoId(Long polideportivoId) { this.polideportivoId = polideportivoId; }

    public String getPolideportivoNombre() { return polideportivoNombre; }
    public void setPolideportivoNombre(String polideportivoNombre) { this.polideportivoNombre = polideportivoNombre; }

    public String getPolideportivoDireccion() { return polideportivoDireccion; }
    public void setPolideportivoDireccion(String polideportivoDireccion) { this.polideportivoDireccion = polideportivoDireccion; }

    public String getHoraApertura() { return horaApertura; }
    public void setHoraApertura(String horaApertura) { this.horaApertura = horaApertura; }

    public String getHoraCierre() { return horaCierre; }
    public void setHoraCierre(String horaCierre) { this.horaCierre = horaCierre; }
}
