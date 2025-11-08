package com.semillero.Constructores.domain.model;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "inventario_faltante")
public class InventarioFaltante {
   @Id
    private String id;

    private LocalDateTime fechaRegistro;

    
    private Map<Material, Integer> faltantes;

    public InventarioFaltante() {}

    public InventarioFaltante(Map<Material, Integer> faltantes) {
        this.fechaRegistro = LocalDateTime.now();
        this.faltantes = faltantes;
    }

    // Getters y setters
    public String getId() { return id; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public Map<Material, Integer> getFaltantes() { return faltantes; }
    public void setFaltantes(Map<Material, Integer> faltantes) { this.faltantes = faltantes; }
}
