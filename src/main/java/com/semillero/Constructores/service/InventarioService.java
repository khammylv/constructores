package com.semillero.Constructores.service;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.semillero.Constructores.DTO.ReponerResultado;
import com.semillero.Constructores.domain.Inventario;
import com.semillero.Constructores.domain.model.InventarioFaltante;
import com.semillero.Constructores.domain.model.Material;
import com.semillero.Constructores.repository.InventarioFaltanteRepository;
import com.semillero.Constructores.repository.InventarioRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Service
public class InventarioService {
    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private InventarioFaltanteRepository inventarioFaltanteRepository;

    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    public Optional<Map<Material, Long>>obtenerTodos() {
        Optional<Inventario> inventario = inventarioRepository.findById("INVENTARIO_UNICO");
        Optional<Map<Material, Long>> materialesOpt = inventario.map(Inventario::getMateriales);
        return materialesOpt;
    }

    public Map<String, Integer> materialesRestantes() {
        List<InventarioFaltante> faltantes = inventarioFaltanteRepository.findAll();
        if (faltantes.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Integer> totalFaltantes = faltantes.stream()
                .flatMap(r -> r.getFaltantes().entrySet().stream())
                .collect(Collectors.toMap(
                        e -> e.getKey().toString(),
                        Map.Entry::getValue,
                        (a, b) -> a + b));

        return totalFaltantes;
    }

    public void consumirMateriales(Map<Material, Integer> materialesAConsumir) {
        Inventario inventario = inventarioRepository.findById("INVENTARIO_UNICO")
                .orElseThrow(() -> new IllegalArgumentException("Inventario no encontrado."));
        System.out.println(inventario);
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

            List<Material> insuficientes = inventario.materialesInsuficientes(materialesAConsumir);

            if (!insuficientes.isEmpty()) {

                Map<Material, Integer> faltantes = materialesAConsumir.entrySet().stream()
                        .filter(entry -> insuficientes.contains(entry.getKey()))
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> entry.getValue()
                                        - inventario.getMateriales().getOrDefault(entry.getKey(), 0L).intValue()));

                inventarioFaltanteRepository.save(new InventarioFaltante(faltantes));

                throw new IllegalArgumentException("No hay suficiente material para realizar la operación.");
            }

            inventario.consumirMateriales(materialesAConsumir);
            inventarioRepository.save(inventario);
        });
    }

    public void reponerMateriales(Map<Material, Long> materialesAReponer) {

        Objects.requireNonNull(materialesAReponer, "El mapa de materiales no puede ser nulo.");
        if (materialesAReponer.isEmpty())
            return;
        Inventario inventario = inventarioRepository.findById("INVENTARIO_UNICO")
                .orElseThrow(() -> new IllegalArgumentException("Inventario no encontrado."));

        materialesAReponer.forEach(inventario::reponerMateriales);
        inventarioRepository.save(inventario);

        inventarioFaltanteRepository.findAll().forEach(faltante -> {
            Map<Material, Integer> faltantes = new HashMap<>(faltante.getFaltantes());

            materialesAReponer.forEach(
                    (material, cantidadRepuesta) -> faltantes.computeIfPresent(material, (m, faltanteActual) -> {
                        Integer nuevoValor = faltanteActual - cantidadRepuesta.intValue();
                        return nuevoValor > 0 ? nuevoValor : null;
                    }));

            if (faltantes.isEmpty()) {
                inventarioFaltanteRepository.delete(faltante);
            } else if (!faltantes.equals(faltante.getFaltantes())) {
                faltante.setFaltantes(faltantes);
                inventarioFaltanteRepository.save(faltante);
            }
        });
    }

    public CompletableFuture<ReponerResultado> reponerMaterialesAsync(Map<Material, Long> materialesAReponer) {

        Objects.requireNonNull(materialesAReponer, "El mapa de materiales no puede ser nulo.");
        if (materialesAReponer.isEmpty()) {
            return CompletableFuture.completedFuture(new ReponerResultado(null, materialesAReponer, List.of()));
        }

        final Map<Material, Long> disponiblesProcesados = new ConcurrentHashMap<>(materialesAReponer);

        return CompletableFuture

                .supplyAsync(() -> {
                    Inventario inventario = inventarioRepository.findById("INVENTARIO_UNICO")
                            .orElseThrow(() -> new IllegalArgumentException("Inventario no encontrado."));

                    materialesAReponer.forEach(inventario::reponerMateriales);
                    return inventarioRepository.save(inventario);
                }, executor)

                .thenComposeAsync(inventarioActualizado -> {

                    List<InventarioFaltante> faltantesExistentes = inventarioFaltanteRepository.findAll();
                    List<CompletableFuture<InventarioFaltante>> tareasFaltantes = new ArrayList<>();

                    for (InventarioFaltante faltante : faltantesExistentes) {
                        CompletableFuture<InventarioFaltante> future = CompletableFuture.supplyAsync(() -> {

                            return procesarFaltantesConsumo(faltante, disponiblesProcesados);
                        }, executor);
                        tareasFaltantes.add(future);
                    }

                    return CompletableFuture.allOf(tareasFaltantes.toArray(new CompletableFuture[0]))
                            .thenApply(v -> {

                                return tareasFaltantes.stream()
                                        .map(CompletableFuture::join)
                                        .toList();
                            });

                }, executor)

                .thenApplyAsync(faltantesProcesados -> {

                    List<InventarioFaltante> faltantesActualizados = new ArrayList<>();
                    for (InventarioFaltante faltante : faltantesProcesados) {
                        if (faltante.getFaltantes().isEmpty()) {
                            inventarioFaltanteRepository.delete(faltante);
                        } else {
                            InventarioFaltante savedFaltante = inventarioFaltanteRepository.save(faltante);
                            faltantesActualizados.add(savedFaltante);
                        }
                    }

                    materialesAReponer.clear();
                    materialesAReponer.putAll(disponiblesProcesados);

                    Inventario inventarioFinal = inventarioRepository.findById("INVENTARIO_UNICO").orElse(null);
                    return new ReponerResultado(inventarioFinal, materialesAReponer, faltantesActualizados);
                }, executor)

                .whenComplete((res, ex) -> {

                    if (ex != null) {
                        throw new CompletionException("Error en el flujo asíncrono de reposición.", ex);
                    }
                });
    }

    /**
     * Procesa el consumo de materiales en un solo InventarioFaltante.
     * * @param faltante El objeto InventarioFaltante a modificar.
     * 
     * @param disponibles El mapa CONCURRENTE de materiales disponibles para
     *                    consumir.
     * @return El InventarioFaltante modificado.
     */
    private InventarioFaltante procesarFaltantesConsumo(InventarioFaltante faltante, Map<Material, Long> disponibles) {
        Map<Material, Integer> faltantesCopia = new HashMap<>(faltante.getFaltantes());

        for (Map.Entry<Material, Integer> entry : faltantesCopia.entrySet()) {
            Material material = entry.getKey();
            long cantidadFaltante = entry.getValue();

            disponibles.computeIfPresent(material, (k, disponibleActual) -> {
                long consumido = Math.min(cantidadFaltante, disponibleActual);
                long restante = disponibleActual - consumido;
                int nuevoFaltante = (int) (cantidadFaltante - consumido);

                if (nuevoFaltante > 0) {
                    faltante.getFaltantes().put(material, nuevoFaltante);
                } else {
                    faltante.getFaltantes().remove(material);
                }

                return restante > 0 ? restante : null;
            });
        }

        return faltante;
    }

}
