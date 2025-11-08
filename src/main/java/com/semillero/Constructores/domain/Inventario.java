package com.semillero.Constructores.domain;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.semillero.Constructores.domain.model.Material;

@Document(collection = "inventario")
public class Inventario {
    @Id
    private String id = "INVENTARIO_UNICO";

   
    private Map<Material, Long> materiales;

    
    public Inventario() {
        this.materiales = new ConcurrentHashMap<>();
    }

   
    public Inventario(Map<Material, Long> cantidadesIniciales) {
        this.materiales = new ConcurrentHashMap<>(cantidadesIniciales);
    }

    public boolean tieneSuficiente(Map<Material, Integer> requerimientos) {
       return requerimientos.entrySet().stream()
            .allMatch(entry -> materiales.getOrDefault(entry.getKey(), 0L) >= entry.getValue());
        
    }

    public List<Material> materialesInsuficientes(Map<Material, Integer> requerimientos) {
    return requerimientos.entrySet().stream()
        .filter(entry -> materiales.getOrDefault(entry.getKey(), 0L) < entry.getValue()) // los que NO alcanzan
        .map(Map.Entry::getKey) 
        .toList(); 
}


  public void consumirMateriales(Map<Material, Integer> requerimientos) {
    requerimientos.forEach((material, consumido) ->
        materiales.computeIfPresent(material, (k, v) -> v - consumido)
    );
}

    public void reponerMateriales(Material material, Long cantidad) {
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

   



    public void reponerMateriales(Material material, long cantidad) {
        materiales.merge(material, cantidad, Long::sum);
    }


}