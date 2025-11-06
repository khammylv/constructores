package com.semillero.Constructores.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.semillero.Constructores.domain.InformePDF;
import com.semillero.Constructores.domain.OrdenConstruccion;
import com.semillero.Constructores.domain.model.Fechas;

@Component
public class PdfJobManager {
    private final Map<String, CompletableFuture<byte[]>> trabajos = new ConcurrentHashMap<>();
    private final Map<String, SseEmitter> emisores = new ConcurrentHashMap<>();
    private final Map<String, Object> resultadosPendientes = new ConcurrentHashMap<>();
    private static final long SSE_TIMEOUT = 0L; // conexión indefinida

    private final Map<String, byte[]> pdfGenerados = new ConcurrentHashMap<>();

    private final OrdenConstruccionService ordenService;

    public PdfJobManager(OrdenConstruccionService ordenService) {
        this.ordenService = ordenService;
    }

    

    //  Inicia el trabajo asincrónico
    public String iniciarGeneracionPdf() {
        String jobId = UUID.randomUUID().toString();
        CompletableFuture<List<OrdenConstruccion>> ordenesFuture = ordenService.listarTodasAsync();
        CompletableFuture<Optional<Fechas>> fechasFuture = ordenService.totalDiasFinalizarAsync();

        CompletableFuture<byte[]> trabajo = ordenesFuture.thenCombine(fechasFuture, (ordenes, optFechas) -> {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                LocalDate fechaInicio = optFechas.flatMap(f -> Optional.ofNullable(f.fechaInicioProgramada()))
                        .orElse(null);
                LocalDate fechaFin = optFechas.flatMap(f -> Optional.ofNullable(f.fechaFinProgramada())).orElse(null);
                InformePDF.generarInformeStream(ordenes, fechaInicio, fechaFin, out);
                return out.toByteArray();
            } catch (Exception e) {
                throw new RuntimeException("Error generando PDF", e);
            }
        });

        trabajos.put(jobId, trabajo);

        trabajo.whenComplete((pdfBytes, error) -> notificarCompletado(jobId, pdfBytes, error));

        System.out.println("Generación PDF registrada: " + jobId);
        return jobId;
    }

    //  Crea el emisor SSE para escuchar el progreso o la finalización
    public SseEmitter crearEmisor(String jobId) {
        CompletableFuture<byte[]> futuro = trabajos.get(jobId);
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        emisores.put(jobId, emitter);

        if (futuro == null && !resultadosPendientes.containsKey(jobId)) {
            try {
                emitter.send(SseEmitter.event().name("error").data(Map.of("status", "NO_ENCONTRADO")));
            } catch (IOException ignored) {
            }
            emitter.complete();
            return emitter;
        }

        // Si está pendiente
        if (futuro != null && !futuro.isDone()) {
            try {
                emitter.send(SseEmitter.event()
                        .name("pendiente")
                        .data(Map.of("status", "PENDIENTE", "jobId", jobId)));
            } catch (IOException ignored) {
            }
        }

        // Si ya terminó antes de conectar
        Object pendiente = resultadosPendientes.remove(jobId);
        if (pendiente instanceof byte[] bytes)
            enviarEvento(emitter, bytes, null);
        else if (pendiente instanceof Throwable err)
            enviarEvento(emitter, null, err);

        emitter.onCompletion(() -> emisores.remove(jobId));
        emitter.onTimeout(() -> {
            emisores.remove(jobId);
            emitter.complete();
        });

        return emitter;
    }

    //  Envía resultado o error
private void notificarCompletado(String jobId, byte[] pdfBytes, Throwable err) {
    SseEmitter emitter = emisores.remove(jobId);

    if (emitter != null)
        enviarEvento(emitter, pdfBytes, err);
    else
        resultadosPendientes.put(jobId, err != null ? err : pdfBytes);

    //  Guardar el PDF en memoria
    if (pdfBytes != null && err == null) {
        pdfGenerados.put(jobId, pdfBytes);
        System.out.println("📄 PDF guardado para descarga: " + jobId);
    }

    trabajos.remove(jobId);
}
    // Envía evento SSE
    private void enviarEvento(SseEmitter emitter, byte[] pdfBytes, Throwable err) {
        try {
            if (err != null) {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data(Map.of("status", "ERROR", "mensaje", err.getMessage())));
            } else {
                emitter.send(SseEmitter.event()
                        .name("completado")
                        .data(Map.of("status", "COMPLETADO")));
            }
        } catch (IOException e) {
            System.err.println("⚠️ Error enviando SSE: " + e.getMessage());
        } finally {
            emitter.complete();
        }
    }

   public byte[] obtenerResultado(String jobId) {
    return pdfGenerados.get(jobId);
}

public void eliminarPdf(String jobId) {
    pdfGenerados.remove(jobId);
}

}
