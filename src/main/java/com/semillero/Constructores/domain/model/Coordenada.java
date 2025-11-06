package com.semillero.Constructores.domain.model;

import java.text.DecimalFormat;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Coordenada {
    private final double x;
    private final double y;

    @JsonCreator
    public Coordenada(@JsonProperty("x") double x, @JsonProperty("y") double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    // Método hashCode y equals crucial para la restricción de coordenadas
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Coordenada coordenada = (Coordenada) o;

        return Double.compare(coordenada.x, x) == 0 && Double.compare(coordenada.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(x, y);
    }

       @Override
    public String toString() {
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(x) + ", " + df.format(y);
    }
}
