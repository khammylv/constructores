package com.semillero.Constructores.components;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.semillero.Constructores.DTO.ReponerResultado;


@Component
public class InventarioJobManager {
    // ReponerResultado
    private final Map<String, CompletableFuture<ReponerResultado>> trabajos = new ConcurrentHashMap<>();
    private final Map<String, SseEmitter> emisores = new ConcurrentHashMap<>();

    private final Map<String, ReponerResultado> trabajosGenerados = new ConcurrentHashMap<>();

    private final Map<String, Object> resultadosPendientes = new ConcurrentHashMap<>();

    private static final long SSE_TIMEOUT = 0L;

    public void registrarTrabajo(String jobId, CompletableFuture<ReponerResultado> futuro) {
        trabajos.put(jobId, futuro);
        System.out.println(" Trabajo registrado: " + jobId);
    }

    public void notificarCompletado(String jobId, ReponerResultado res, Throwable err) {
        SseEmitter emitter = emisores.remove(jobId);

        if (emitter != null) {
            enviarEvento(emitter, res, err);
        } else {
            // Guardar resultado o error pendiente
            resultadosPendientes.put(jobId, err != null ? err : res);
        }

        if (res != null && err == null) {
            trabajosGenerados.put(jobId, res);

        }

        trabajos.remove(jobId);
    }

    public SseEmitter crearEmisor(String jobId) {
        CompletableFuture<ReponerResultado> futuro = trabajos.get(jobId);

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
            if (pendiente instanceof ReponerResultado) {
                enviarEvento(emitter, (ReponerResultado) pendiente, null);
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

    private void enviarEvento(SseEmitter emitter, ReponerResultado res, Throwable err) {
        try {
            if (err != null) {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data(Map.of("status", "ERROR", "mensaje", err.getMessage())));
            } else {
                emitter.send(SseEmitter.event()
                        .name("completado")
                        .data(Map.of("status", "COMPLETADO", "ordenId", res)));
            }
        } catch (IOException e) {
            System.err.println("⚠️ Error enviando SSE: " + e.getMessage());
        } finally {
            emitter.complete();
        }
    }

     public ReponerResultado obtenerResultado(String jobId) {
        return trabajosGenerados.get(jobId);
    }

    public void eliminarResultado(String jobId) {
        trabajosGenerados.remove(jobId);
    }
}
