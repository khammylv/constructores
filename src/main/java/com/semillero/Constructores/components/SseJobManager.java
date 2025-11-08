package com.semillero.Constructores.components;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.semillero.Constructores.domain.OrdenConstruccion;

/**
 * SSE y Cliente EventSource: La implementación de SSE en los navegadores
 * (clase EventSource en JavaScript) tiene una lógica de reconexión automática
 * nativa.
 * Si la conexión GET falla, el cliente automáticamente intenta reconectarse.
 * Esto no existe para la respuesta POST original
 */

@Component
public class SseJobManager {
    // Mapas concurrentes para trabajo y emisores
    private final Map<String, CompletableFuture<OrdenConstruccion>> trabajos = new ConcurrentHashMap<>();
    private final Map<String, SseEmitter> emisores = new ConcurrentHashMap<>();
   

    private final Map<String, Object> resultadosPendientes = new ConcurrentHashMap<>();

    private static final long SSE_TIMEOUT = 0L; // conexión indefinida

    // Registrar tarea
    public void registrarTrabajo(String jobId, CompletableFuture<OrdenConstruccion> futuro) {
        trabajos.put(jobId, futuro);
        System.out.println(" Trabajo registrado: " + jobId);
    }

    // Notificar finalización


    public void notificarCompletado(String jobId, OrdenConstruccion res, Throwable err) {
        SseEmitter emitter = emisores.remove(jobId);

        if (emitter != null) {
            enviarEvento(emitter, res, err);
        } else {
            // Guardar resultado o error pendiente
            resultadosPendientes.put(jobId, err != null ? err : res);
        }

        trabajos.remove(jobId);
    }

 

    public SseEmitter crearEmisor(String jobId) {
        CompletableFuture<OrdenConstruccion> futuro = trabajos.get(jobId);

        if (futuro == null && !resultadosPendientes.containsKey(jobId)) {
            // Trabajo no existe
            System.out.println("⚠️ JobId no encontrado: " + jobId);
            SseEmitter errorEmitter = new SseEmitter(SSE_TIMEOUT);
            try {
                errorEmitter.send(SseEmitter.event()
                        .name("error")
                        .data(Map.of("status", "NO_ENCONTRADO")));
            } catch (IOException ignored) {
            }
            errorEmitter.complete();
            return errorEmitter;
        }

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        emisores.put(jobId, emitter);

        // Enviar estado inicial
        if (futuro != null && !futuro.isDone()) {
            try {
                emitter.send(SseEmitter.event()
                        .name("pendiente")
                        .data(Map.of("status", "PENDIENTE", "jobId", jobId)));
            } catch (IOException ignored) {
            }
        }

        // Si hay resultado pendiente, enviarlo inmediatamente
        Object pendiente = resultadosPendientes.remove(jobId);

        if (pendiente != null) {
            if (pendiente instanceof OrdenConstruccion) {
                enviarEvento(emitter, (OrdenConstruccion) pendiente, null);
            } else if (pendiente instanceof Throwable) {
                enviarEvento(emitter, null, (Throwable) pendiente);
            }
        }
        // Configurar limpieza
        emitter.onCompletion(() -> {
            System.out.println("🔗 SSE completado para job " + jobId);
            emisores.remove(jobId);
        });
        emitter.onTimeout(() -> {
            System.out.println("⌛ Timeout SSE para job " + jobId);
            emisores.remove(jobId);
            emitter.complete();
        });

        return emitter;
    }



    private void enviarEvento(SseEmitter emitter, OrdenConstruccion res, Throwable err) {
        try {
            if (err != null) {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data(Map.of("status", "ERROR", "mensaje", err.getMessage())));
            } else {
                emitter.send(SseEmitter.event()
                        .name("completado")
                        .data(Map.of("status", "COMPLETADO", "ordenId", res.getId())));
            }
        } catch (IOException e) {
            System.err.println("⚠️ Error enviando SSE: " + e.getMessage());
        } finally {
            emitter.complete();
        }
    }

}
