package com.deustosport.my_app.enums;

/**
 * Clasificación visual de un aviso del día publicado por secretaría.
 *
 * Cada nivel determina el color y el icono que se mostrará en el banner
 * de la pantalla de inicio de los usuarios.
 *
 *  INFO     → azul/neutro  — información general sin urgencia
 *  AVISO    → naranja/warn — precaución, algo fuera de lo habitual
 *  URGENTE  → rojo/danger  — impacto directo en el uso de instalaciones
 */
public enum TipoAviso {

    /**
     * Aviso puramente informativo. No implica ninguna restricción en el
     * uso de instalaciones. Se usa para comunicar novedades, cambios de
     * horario puntuales o recordatorios de interés general.
     *
     * Ejemplo: "Mañana lunes el centro cierra a las 20:00 h."
     */
    INFO,

    /**
     * Aviso de precaución. Indica que algún servicio o zona del centro
     * opera de manera diferente a lo habitual, pero el impacto en el
     * usuario es parcial o secundario.
     *
     * Ejemplo: "El aparcamiento exterior estará cortado hasta las 12:00 h."
     */
    AVISO,

    /**
     * Aviso urgente. Comunica una incidencia que afecta directamente al
     * uso normal de las instalaciones deportivas. Debe mostrarse de forma
     * destacada y con mayor prominencia visual que los niveles anteriores.
     *
     * Ejemplo: "Duchas fuera de servicio por avería. Técnicos ya en el centro."
     */
    URGENTE
}
