package com.semillero.Constructores.domain.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "inventario")
public class Inventario {
      @Id
    private String id = "INVENTARIO_UNICO";

    // Guardamos las cantidades como Long, usando el nombre del enum como clave
    private Map<Material, Long> materiales;

    // Constructor vacío requerido por MongoDB
    public Inventario() {
        this.materiales = new ConcurrentHashMap<>();
    }

    // Constructor con valores iniciales
    public Inventario(Map<Material, Long> cantidadesIniciales) {
        this.materiales = new ConcurrentHashMap<>(cantidadesIniciales);
    }

    public boolean tieneSuficiente(Map<Material, Integer> requerimientos) {
        for (Map.Entry<Material, Integer> entry : requerimientos.entrySet()) {
            Material material = entry.getKey();
            int requerido = entry.getValue();

            if (materiales.getOrDefault(material, 0L) < requerido) {
                return false;
            }
        }
        return true; 
    }

    public void consumirMateriales(Map<Material, Integer> requerimientos) {
        for (Map.Entry<Material, Integer> entry : requerimientos.entrySet()) {
            Material material = entry.getKey();
            int consumido = entry.getValue();
            materiales.computeIfPresent(material, (k, v) -> v - consumido);
        }
    }

    public void reponerMateriales(Material material, long cantidad) {
        materiales.merge(material, cantidad, Long::sum);
    }

    public Map<Material, Long> getCantidadesActuales() {
        return new ConcurrentHashMap<>(materiales);
    }

    public String getId() {
        return id;
    }

    public Map<Material, Long> getMateriales() {
        return materiales;
    }

    public void setMateriales(Map<Material, Long> materiales) {
        this.materiales = materiales;
    }
}