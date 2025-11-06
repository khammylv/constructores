package com.semillero.Constructores.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.semillero.Constructores.domain.model.Inventario;
import com.semillero.Constructores.domain.model.Material;
import com.semillero.Constructores.repository.InventarioRepository;
import com.semillero.Constructores.service.InventarioService;

@RestController
@RequestMapping("api/inventario")
public class InventarioController {
    @Autowired
    private InventarioRepository inventarioRepository;

   

    @GetMapping
    public Optional<Inventario> obtenerInventario() {
        return inventarioRepository.findById("INVENTARIO_UNICO");
    }

    @PutMapping("/reponer")
    public String reponerMateriales(@RequestBody Map<Material, Long> materialesAReponer) {
        Inventario inventario = inventarioRepository.findById("INVENTARIO_UNICO")
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));

        materialesAReponer.forEach(inventario::reponerMateriales);
        inventarioRepository.save(inventario);

        return "Materiales repuestos exitosamente.";
    }

    @PutMapping("/consumir")
    public String consumirMateriales(@RequestBody Map<Material, Integer> materialesAConsumir) {
        Inventario inventario = inventarioRepository.findById("INVENTARIO_UNICO")
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));

        if (!inventario.tieneSuficiente(materialesAConsumir)) {
            return " No hay suficiente material para realizar la operación.";
        }

        inventario.consumirMateriales(materialesAConsumir);
        inventarioRepository.save(inventario);

        return "✅ Materiales consumidos exitosamente.";
    }
    
}
