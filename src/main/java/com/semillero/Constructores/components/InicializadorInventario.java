package com.semillero.Constructores.components;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.semillero.Constructores.domain.Inventario;
import com.semillero.Constructores.domain.model.Material;
import com.semillero.Constructores.repository.InventarioRepository;

import jakarta.annotation.PostConstruct;

@Component
public class InicializadorInventario {
 @Autowired
    private InventarioRepository inventarioRepository;

      @PostConstruct
    public void inicializarInventario() {
      
        if (inventarioRepository.existsById("INVENTARIO_UNICO")) {
            System.out.println("✅ Inventario ya existe en la base de datos");
            return;
        }

        // Crear inventario inicial
        Map<Material, Long> materialesIniciales = Map.of(
                Material.CEMENTO, 100L,
                Material.GRAVA, 80L,
                Material.ARENA, 200L,
                Material.MADERA, 50L,
                Material.ADOBE, 30L
        );

        Inventario inventario = new Inventario(materialesIniciales);
        inventarioRepository.save(inventario);
        System.out.println("✅ Inventario inicial guardado en MongoDB");
    }  
}
