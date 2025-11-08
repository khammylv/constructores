package com.semillero.Constructores.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.semillero.Constructores.domain.Inventario;
import com.semillero.Constructores.domain.model.InventarioFaltante;
import com.semillero.Constructores.domain.model.Material;
import com.semillero.Constructores.service.InventarioService;

@RestController
@RequestMapping("/api/test")
public class testController {

    @Autowired
    private InventarioService inventarioService;

    @GetMapping
    public ResponseEntity<Map<String, Optional<Map<Material, Long>>>> obtenerInventario() {

        return ResponseEntity.ok(Map.of("materiales", inventarioService.obtenerTodos()));
    }

    @GetMapping("/faltantes")
    public ResponseEntity<Map<String, Map<String, Integer>>> obtenerInventarioFaltante() {

        return ResponseEntity.ok(Map.of("faltantes", inventarioService.materialesRestantes()));
    }

}
