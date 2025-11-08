package com.semillero.Constructores.controller;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.semillero.Constructores.DTO.OrdenRequest;
import com.semillero.Constructores.DTO.ReponerResultado;
import com.semillero.Constructores.components.InventarioJobManager;
import com.semillero.Constructores.domain.Inventario;
import com.semillero.Constructores.domain.OrdenConstruccion;
import com.semillero.Constructores.domain.model.Material;
import com.semillero.Constructores.repository.InventarioRepository;
import com.semillero.Constructores.service.InventarioService;

@RestController
@RequestMapping("api/inventario")
public class InventarioController {
    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private InventarioService inventarioService;

    private final InventarioJobManager sseJobManager;

    public InventarioController(InventarioJobManager sseJobManager) {
        this.sseJobManager = sseJobManager;
    }

 @GetMapping
    public ResponseEntity<Map<String, Optional<Map<Material, Long>>>> obtenerInventario() {

        return ResponseEntity.ok(Map.of("materiales", inventarioService.obtenerTodos()));
    }

    @GetMapping("/faltantes")
    public ResponseEntity<Map<String, Map<String, Integer>>> obtenerInventarioFaltante() {

        return ResponseEntity.ok(Map.of("faltantes", inventarioService.materialesRestantes()));
    }

    // @PutMapping("/reponer")
    // public ResponseEntity<Map<String, String>> reponerMateriales(@RequestBody
    // Map<Material, Long> materialesAReponer) {
    // this.inventarioService.reponerMateriales(materialesAReponer);
    // return ResponseEntity.ok(Map.of("message", "Materiales repuestos
    // exitosamente."));
    // }

    @PutMapping("/reponer")
    public ResponseEntity<Map<String, String>> reponerMateriales(@RequestBody Map<Material, Long> materialesAReponer) {

        // Generar un jobId único
        String jobId = UUID.randomUUID().toString();

        // Ejecutar la tarea asíncrona desde el service
        // CompletableFuture<OrdenConstruccion> futuro =
        // service.procesarOrdenAsync(request, jobId);
        CompletableFuture<ReponerResultado> futuro = inventarioService.reponerMaterialesAsync(materialesAReponer);

        // Registrar el trabajo en SseJobManager
        sseJobManager.registrarTrabajo(jobId, futuro);

        // Cuando termine, notificar al cliente vía SSE
        futuro.whenComplete((res, err) -> sseJobManager.notificarCompletado(jobId,
                res, err));

        // Respuesta inmediata al cliente
        return ResponseEntity.accepted().body(Map.of(
                "jobId", jobId,
                "status", "ACCEPTED",
                "message", "La orden está en cola y será procesada. Use /api/inventario/status/"
                        + jobId + " para monitorear."));
    }

    @GetMapping("/status/{jobId}")
    public SseEmitter streamEstado(@PathVariable String jobId) {
        // Delegamos toda la lógica al SseJobManager
        return sseJobManager.crearEmisor(jobId);
    }

    @GetMapping("/inventario-restante/{jobId}")
    public ResponseEntity<Map<String, ReponerResultado>> descargarPdfEncriptado(@PathVariable String jobId) {

        ReponerResultado resultado = sseJobManager.obtenerResultado(jobId);
        if (resultado == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of("Inventario faltante:", resultado));

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
