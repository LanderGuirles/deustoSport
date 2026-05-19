package com.deustosport.my_app.dto;

import java.time.LocalDate;
import java.util.List;

public class DisponibilidadPolideportivoDTO {

    private Long polideportivoId;
    private String polideportivoNombre;
    private String direccion;
    private String horaApertura;
    private String horaCierre;
    private LocalDate fecha;
    private int totalPistas;
    private int pistasActivas;
    private List<DisponibilidadPistaDTO> pistas;

    public DisponibilidadPolideportivoDTO() {}

    public Long getPolideportivoId() { return polideportivoId; }
    public void setPolideportivoId(Long polideportivoId) { this.polideportivoId = polideportivoId; }

    public String getPolideportivoNombre() { return polideportivoNombre; }
    public void setPolideportivoNombre(String polideportivoNombre) { this.polideportivoNombre = polideportivoNombre; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getHoraApertura() { return horaApertura; }
    public void setHoraApertura(String horaApertura) { this.horaApertura = horaApertura; }

    public String getHoraCierre() { return horaCierre; }
    public void setHoraCierre(String horaCierre) { this.horaCierre = horaCierre; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public int getTotalPistas() { return totalPistas; }
    public void setTotalPistas(int totalPistas) { this.totalPistas = totalPistas; }

    public int getPistasActivas() { return pistasActivas; }
    public void setPistasActivas(int pistasActivas) { this.pistasActivas = pistasActivas; }

    public List<DisponibilidadPistaDTO> getPistas() { return pistas; }
    public void setPistas(List<DisponibilidadPistaDTO> pistas) { this.pistas = pistas; }
}
