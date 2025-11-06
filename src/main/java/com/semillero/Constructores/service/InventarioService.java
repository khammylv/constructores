package com.semillero.Constructores.service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;

import com.semillero.Constructores.domain.model.Inventario;
import com.semillero.Constructores.domain.model.Material;
import com.semillero.Constructores.repository.InventarioRepository;

@Service
public class InventarioService {
    private final InventarioRepository inventarioRepository;

    public InventarioService(InventarioRepository inventarioRepository) {
        this.inventarioRepository = inventarioRepository;
    }

    public void consumirMateriales(Map<Material, Integer> materialesAConsumir) {
        Inventario inventario = inventarioRepository.findById("INVENTARIO_UNICO")
                .orElseThrow(() -> new IllegalArgumentException("Inventario no encontrado."));

        if (!inventario.tieneSuficiente(materialesAConsumir)) {
            throw new IllegalArgumentException("No hay suficiente material para realizar la operación.");
        }

        inventario.consumirMateriales(materialesAConsumir);
        inventarioRepository.save(inventario);
    }

    public CompletableFuture<Void> consumirMaterialesAsync(Map<Material, Integer> materialesAConsumir) {
        return CompletableFuture.runAsync(() -> {
            Inventario inventario = inventarioRepository.findById("INVENTARIO_UNICO")
                    .orElseThrow(() -> new IllegalArgumentException("Inventario no encontrado."));

            if (!inventario.tieneSuficiente(materialesAConsumir)) {
                throw new IllegalArgumentException("No hay suficiente material para realizar la operación.");
            }

            inventario.consumirMateriales(materialesAConsumir);
            inventarioRepository.save(inventario);
        });
    }
}
