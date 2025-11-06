package com.semillero.Constructores.domain;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.itextpdf.html2pdf.HtmlConverter;
import com.semillero.Constructores.domain.model.EstadoOrden;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.OutputStream;

public class InformePDF {
 private static final Map<EstadoOrden, String> NOMBRES_ESTADO = Map.of(
        EstadoOrden.PENDIENTE, "Pendiente",
        EstadoOrden.EN_PROGRESO, "En progreso",
        EstadoOrden.FINALIZADO, "Finalizado"
        
);

    // Nuevo método que genera el PDF en un OutputStream

  public static void generarInformeStream(List<OrdenConstruccion> ordenes,
                                            LocalDate fechaInicio,
                                            LocalDate fechaFin,
                                            OutputStream out) {
        try {
            DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            StringBuilder html = new StringBuilder();
            html.append("<html><head>")
                .append("<style>")
                .append("body { font-family: Arial, sans-serif; }")
                .append("h1, h2, h3 { text-align: center; }")
                .append("h2 { color: #2F4F4F; margin-top: 30px; }")
                .append("table { width: 100%; border-collapse: collapse; margin-top: 10px; }")
                .append("th, td { border: 1px solid black; padding: 6px; text-align: left; font-size: 12px; }")
                .append("th { background-color: #f2f2f2; }")
                .append("</style>")
                .append("</head><body>")
                .append("<h1>Informe de Órdenes de Construcción</h1>");

            // Encabezado de fechas
            if (fechaInicio != null && fechaFin != null) {
                html.append("<h3>Periodo: ")
                    .append(df.format(fechaInicio))
                    .append(" - ")
                    .append(df.format(fechaFin))
                    .append("</h3>");
            }

            // Agrupar por estado
            Map<EstadoOrden, List<OrdenConstruccion>> agrupadas =
                    ordenes.stream().collect(Collectors.groupingBy(OrdenConstruccion::getEstado));

            // Recorrer cada grupo
            for (Map.Entry<EstadoOrden, List<OrdenConstruccion>> entry : agrupadas.entrySet()) {
                EstadoOrden estado = entry.getKey();
                List<OrdenConstruccion> grupo = entry.getValue();
                String nombreEstado = NOMBRES_ESTADO.getOrDefault(estado, estado.name());

                html.append("<h2>Órdenes ").append(nombreEstado).append("</h2>")
                    .append("<table>")
                    .append("<tr>")
                    .append("<th>ID</th>")
                    .append("<th>Tipo Construcción</th>")
                    .append("<th>Coordenada</th>")
                    .append("<th>Fecha Solicitud</th>")
                    .append("<th>Fecha Inicio</th>")
                    .append("<th>Fecha Fin</th>")
                    .append("<th>Estado</th>")
                    .append("</tr>");

                for (OrdenConstruccion orden : grupo) {
                    html.append("<tr>")
                        .append("<td>").append(orden.getId()).append("</td>")
                        .append("<td>").append(orden.getTipo() != null ? orden.getTipo().getNombre() : "-").append("</td>")
                        .append("<td>").append(orden.getCoordenada() != null
                                ? orden.getCoordenada().getX() + ", " + orden.getCoordenada().getY() : "-").append("</td>")
                        .append("<td>").append(orden.getFechaSolicitud() != null
                                ? df.format(orden.getFechaSolicitud()) : "-").append("</td>")
                        .append("<td>").append(orden.getFechaInicioProgramada() != null
                                ? df.format(orden.getFechaInicioProgramada()) : "-").append("</td>")
                        .append("<td>").append(orden.getFechaFinProgramada() != null
                                ? df.format(orden.getFechaFinProgramada()) : "-").append("</td>")
                        .append("<td>").append(NOMBRES_ESTADO.getOrDefault(estado, estado.name())).append("</td>")
                        .append("</tr>");
                }

                html.append("</table>");
            }

            html.append("</body></html>");

            // Convertir HTML a PDF
            HtmlConverter.convertToPdf(html.toString(), out);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
