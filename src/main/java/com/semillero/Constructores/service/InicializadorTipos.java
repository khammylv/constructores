package com.semillero.Constructores.service;

import java.util.List;
import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.semillero.Constructores.domain.TipoConstruccion;
import com.semillero.Constructores.domain.model.Material;
import com.semillero.Constructores.repository.TipoConstruccionRepository;

@Component
public class InicializadorTipos  implements CommandLineRunner{
 private final TipoConstruccionRepository repo;

    public InicializadorTipos(TipoConstruccionRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) {
        if (repo.count() == 0) {
            repo.saveAll(
                List.of(
                    new TipoConstruccion("Casa", Map.of(
                        Material.CEMENTO, 100, Material.GRAVA, 50, Material.ARENA, 90,
                        Material.MADERA, 20, Material.ADOBE, 100), 3),
                    new TipoConstruccion("Lago", Map.of(
                        Material.CEMENTO, 50, Material.GRAVA, 60, Material.ARENA, 80,
                        Material.MADERA, 10, Material.ADOBE, 20), 2),
                    new TipoConstruccion("Cancha de Fútbol", Map.of(
                        Material.CEMENTO, 20, Material.GRAVA, 20, Material.ARENA, 20,
                        Material.MADERA, 20, Material.ADOBE, 20), 1),
                    new TipoConstruccion("Edificio", Map.of(
                        Material.CEMENTO, 200, Material.GRAVA, 100, Material.ARENA, 180,
                        Material.MADERA, 40, Material.ADOBE, 200), 6),
                    new TipoConstruccion("Gimnasio", Map.of(
                        Material.CEMENTO, 50, Material.GRAVA, 25, Material.ARENA, 45,
                        Material.MADERA, 10, Material.ADOBE, 50), 2)
                )
            );
        }
    }
}
