package com.deustosport.my_app.dto;

import com.deustosport.my_app.enums.TipoDeporte;
import java.util.List;
import java.util.Map;

public class DisponibilidadPistaDTO {

    private Long pistaId;
    private String pistaNombre;
    private TipoDeporte tipoDeporte;
    private int maxJugadores;
    private boolean activa;
    private List<Map<String, String>> slotsOcupados;

    public DisponibilidadPistaDTO() {}

    public Long getPistaId() { return pistaId; }
    public void setPistaId(Long pistaId) { this.pistaId = pistaId; }

    public String getPistaNombre() { return pistaNombre; }
    public void setPistaNombre(String pistaNombre) { this.pistaNombre = pistaNombre; }

    public TipoDeporte getTipoDeporte() { return tipoDeporte; }
    public void setTipoDeporte(TipoDeporte tipoDeporte) { this.tipoDeporte = tipoDeporte; }

    public int getMaxJugadores() { return maxJugadores; }
    public void setMaxJugadores(int maxJugadores) { this.maxJugadores = maxJugadores; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }

    public List<Map<String, String>> getSlotsOcupados() { return slotsOcupados; }
    public void setSlotsOcupados(List<Map<String, String>> slotsOcupados) { this.slotsOcupados = slotsOcupados; }
}
