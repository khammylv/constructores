package com.semillero.Constructores.domain.model;

public enum Material {
   CEMENTO("Ce"),
    GRAVA("Gr"),
    ARENA("Ar"),
    MADERA("Ma"),
    ADOBE("Ad");

    private final String abreviacion;

    Material(String abreviacion) {
        this.abreviacion = abreviacion;
    }

    public String getAbreviacion() {
        return abreviacion;
    }
}
