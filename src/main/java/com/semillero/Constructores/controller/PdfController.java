package com.semillero.Constructores.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.semillero.Constructores.service.PdfJobManager;
import org.springframework.http.MediaType;
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/pdf")
public class PdfController {
   private final PdfJobManager jobManager;

    @Autowired
    public PdfController(PdfJobManager jobManager) {
        this.jobManager = jobManager;
    }

    // 🔹 Iniciar generación asincrónica
    @PostMapping("/generar")
    public ResponseEntity<Map<String, String>> generarPdfAsync() {
        String jobId = jobManager.iniciarGeneracionPdf();
        return ResponseEntity.ok(Map.of("jobId", jobId));
    }

    // 🔹 Suscribirse al estado SSE
    @GetMapping("/stream/{jobId}")
    public SseEmitter stream(@PathVariable String jobId) {
        return jobManager.crearEmisor(jobId);
    }

    //  Descargar PDF cuando esté listo
    @GetMapping("/descargar/{jobId}")
   public ResponseEntity<byte[]> descargarPdf(@PathVariable String jobId) {
    byte[] pdf = jobManager.obtenerResultado(jobId);
    if (pdf == null) return ResponseEntity.notFound().build();

    // Opcional: eliminar después de descargar
     jobManager.eliminarPdf(jobId);

    return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=informe_async.pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf);
}
}
