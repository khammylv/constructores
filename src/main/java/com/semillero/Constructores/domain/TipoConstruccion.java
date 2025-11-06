package com.semillero.Constructores.domain;

import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.semillero.Constructores.domain.model.Coordenada;
import com.semillero.Constructores.domain.model.Material;

@Document(collection = "tipo_construccion")
public class TipoConstruccion {
    @Id
    private String id;
    private String nombre;
    private Map<Material, Integer> requerimientos;
    private int duracionDias;

    public TipoConstruccion() {
    }

    public TipoConstruccion(String nombre, Map<Material, Integer> requerimientos, int duracionDias) {
        this.nombre = nombre;
        this.requerimientos = requerimientos;
        this.duracionDias = duracionDias;
    }

    public TipoConstruccion(String nombre) {
        this.nombre = nombre;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Map<Material, Integer> getRequerimientos() {
        return requerimientos;
    }

    public int getDuracionDias() {
        return duracionDias;
    }

    @Override
    public String toString() {
        return nombre + " (" + requerimientos + ") => " + duracionDias + "d";
    }

    public int sumar() {
        return duracionDias + 2;
    }

}
