package com.semillero.Constructores.controller;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.itextpdf.io.source.OutputStream;
import com.semillero.Constructores.DTO.OrdenRequest;
import com.semillero.Constructores.domain.InformePDF;
import com.semillero.Constructores.domain.OrdenConstruccion;
import com.semillero.Constructores.domain.TipoConstruccion;
import com.semillero.Constructores.domain.model.Coordenada;
import com.semillero.Constructores.domain.model.EstadoOrden;
import com.semillero.Constructores.domain.model.EstadoOrdenTotal;
import com.semillero.Constructores.domain.model.Fechas;
import com.semillero.Constructores.service.OrdenConstruccionService;
import com.semillero.Constructores.service.OrdenesData;
import com.semillero.Constructores.service.PageResponse;
import com.semillero.Constructores.service.SseJobManager;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("api/ordenes")
public class OrdenConstruccionController {

    private final OrdenConstruccionService ordenService;
    private final SseJobManager sseJobManager;

    public OrdenConstruccionController(OrdenConstruccionService service, SseJobManager sseJobManager) {
        this.ordenService = service;
        this.sseJobManager = sseJobManager;

    }

    @GetMapping("/test")
    public String hola() {

        ordenService.imprimirTotalesPorEstado();
        return "Hola";
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> crearOrden(@RequestBody OrdenRequest request) {

        // Generar un jobId único
        String jobId = UUID.randomUUID().toString();

        // Ejecutar la tarea asíncrona desde el service
        // CompletableFuture<OrdenConstruccion> futuro =
        // service.procesarOrdenAsync(request, jobId);
        CompletableFuture<OrdenConstruccion> futuro = ordenService.procesarOrdenAsyncV2(request, jobId);

        // Registrar el trabajo en SseJobManager
        sseJobManager.registrarTrabajo(jobId, futuro);

        // Cuando termine, notificar al cliente vía SSE
        futuro.whenComplete((res, err) -> sseJobManager.notificarCompletado(jobId,
                res, err));

        // Respuesta inmediata al cliente
        return ResponseEntity.accepted().body(Map.of(
                "jobId", jobId,
                "status", "ACCEPTED",
                "message", "La orden está en cola y será procesada. Use /api/ordenes/status/"
                        + jobId + " para monitorear."));
    }

    // ==================================================
    // 2️⃣ Monitorear estado de la orden (GET SSE)
    // ==================================================
    @GetMapping("/status/{jobId}")
    public SseEmitter streamEstado(@PathVariable String jobId) {
        // Delegamos toda la lógica al SseJobManager
        return sseJobManager.crearEmisor(jobId);
    }

    @GetMapping("/fechas-finalizacion")
    public CompletableFuture<ResponseEntity<?>> obtenerFechasFinalizacion() {
        return ordenService.totalDiasFinalizarAsync()
                .thenApply(optFechas -> optFechas
                        .<ResponseEntity<?>>map(ResponseEntity::ok)
                        .orElseGet(() -> ResponseEntity
                                .status(HttpStatus.NO_CONTENT)
                                .body("No hay órdenes registradas.")));
    }

    // Listar todas las órdenes
    @GetMapping
    public OrdenesData<OrdenConstruccion> listarTodas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ordenService.listarTodasPaginator(page, size);
    }

    @GetMapping("/estado-totales")
    public List<EstadoOrdenTotal> listarSumaEstados() {
        return ordenService.imprimirTotalesPorEstado();
    }
    // obtenerTipoConstruccionAsync

    @GetMapping("/estado/{estado}")
    public CompletableFuture<ResponseEntity<?>> listarPorEstado(@PathVariable EstadoOrden estado) {
        return ordenService.obtenerTipoConstruccionAsync(estado)
                .thenApply(lista -> lista.isEmpty()
                        ? ResponseEntity.status(HttpStatus.NO_CONTENT)
                                .body("No hay órdenes registradas.")
                        : ResponseEntity.ok(lista));
    }

    @GetMapping("/test/pdf")
    public CompletableFuture<Void> generarPdf(HttpServletResponse response) {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=informe.pdf");

        CompletableFuture<List<OrdenConstruccion>> ordenesFuture = ordenService.listarTodasAsync();
        CompletableFuture<Optional<Fechas>> fechasFuture = ordenService.totalDiasFinalizarAsync();

        return ordenesFuture.thenCombine(fechasFuture, (ordenes, optFechas) -> {
            try (ServletOutputStream out = response.getOutputStream()) {
                LocalDate fechaInicio = null;
                LocalDate fechaFin = null;

                if (optFechas.isPresent()) {
                    fechaInicio = optFechas.get().fechaInicioProgramada();
                    fechaFin = optFechas.get().fechaFinProgramada();
                }

                InformePDF.generarInformeStream(ordenes, fechaInicio, fechaFin, out);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
    }

}
