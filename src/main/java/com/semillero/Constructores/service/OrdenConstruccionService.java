package com.semillero.Constructores.service;

import java.time.LocalDate;
import java.util.List;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


import com.semillero.Constructores.DTO.OrdenRequest;
import com.semillero.Constructores.domain.OrdenConstruccion;
import com.semillero.Constructores.domain.TipoConstruccion;
import com.semillero.Constructores.domain.model.Coordenada;
import com.semillero.Constructores.domain.model.EstadoOrden;
import com.semillero.Constructores.domain.model.EstadoOrdenTotal;
import com.semillero.Constructores.domain.model.Fechas;
import com.semillero.Constructores.repository.OrdenConstruccionRepository;
import com.semillero.Constructores.repository.TipoConstruccionRepository;

@Service
public class OrdenConstruccionService {

    @Autowired
    private TipoConstruccionRepository tipoRepo;

    @Autowired
    private OrdenConstruccionRepository ordenRepository;

    @Autowired

    private ExecutorService orderProcessingExecutor;

    @Autowired
    private InventarioService inventarioService;

    /**
     * Crea y guarda una nueva orden de construcción.
     */
    public OrdenConstruccion crearOrden(TipoConstruccion tipo, Coordenada coordenada) {
        OrdenConstruccion orden = new OrdenConstruccion(tipo, coordenada, LocalDate.now());
        return ordenRepository.save(orden);
    }

    /**
     * Obtiene todas las órdenes registradas.
     */
    public List<OrdenConstruccion> listarTodas() {
        return ordenRepository.findAll();
    }
  public OrdenesData<OrdenConstruccion> listarTodasPaginator(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<OrdenConstruccion> pageResult = ordenRepository.findAll(pageable);

        OrdenesData<OrdenConstruccion> response = new OrdenesData<>();
        response.setPageIndex(pageResult.getNumber());
        response.setPageSize(pageResult.getSize());
        response.setTotalCount(pageResult.getTotalElements());
        response.setTotalPages(pageResult.getTotalPages());
        response.setData(pageResult.getContent());

        return response;
    }

    public CompletableFuture<Optional<Fechas>> totalDiasFinalizarAsync() {
        return CompletableFuture.supplyAsync(() -> {
            List<OrdenConstruccion> ordenes = listarTodas();

            return ordenes.stream().findFirst().map(primeraOrden -> {
                int totalDias = ordenes.stream()
                        .mapToInt(orden -> orden.getTipo().sumar())
                        .sum();

                LocalDate fechaInicio = primeraOrden.getFechaSolicitud();
                LocalDate fechaFin = fechaInicio.plusDays(totalDias);

                ;

                return new Fechas(fechaInicio, fechaFin);
            });
        }, orderProcessingExecutor);
    }

    public List<EstadoOrdenTotal> imprimirTotalesPorEstado() {
        return ordenRepository.contarPorEstado();
    }

    /**
     * Busca órdenes por estado.
     */
    public List<OrdenConstruccion> listarPorEstado(EstadoOrden estado) {
        return ordenRepository.findByEstado(estado);
    }

    public CompletableFuture<List<OrdenConstruccion>> obtenerTipoConstruccionAsync(EstadoOrden estado) {
        return CompletableFuture.supplyAsync(() -> {
            return ordenRepository.findByEstado(estado);
        });
    }

    public CompletableFuture<List<OrdenConstruccion>> listarTodasAsync() {
        return CompletableFuture.supplyAsync(() -> {
            return ordenRepository.findAll();
        });
    }

    private boolean existePorCoordenada(Coordenada coordenada) {
        return ordenRepository.findAll()
                .stream()
                .anyMatch(o -> o.getCoordenada().equals(coordenada));
    }

    public void validarCoordenada(Coordenada coordenada) {
        if (existePorCoordenada(coordenada)) {
            throw new IllegalArgumentException("Ya existe una orden en esa coordenada.");
        }
    }

    public CompletableFuture<Boolean> existePorCoordenadaAsync(Coordenada coordenada) {
        return CompletableFuture.supplyAsync(() -> ordenRepository.findAll()
                .stream()
                .anyMatch(o -> o.getCoordenada().equals(coordenada)));
    }

    public CompletableFuture<Void> validarCoordenadaAsync(Coordenada coordenada) {
        return existePorCoordenadaAsync(coordenada)
                .thenAccept(existe -> {
                    if (existe) {
                        throw new IllegalArgumentException("Ya existe una orden en esa coordenada.");
                    }
                });
    }

    public TipoConstruccion obtenerTipoConstruccion(String tipoNombre) {

        TipoConstruccion tipoConstruccion = tipoRepo.findByNombreIgnoreCase(tipoNombre)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tipo de construcción no encontrado: " + tipoNombre));
        return tipoConstruccion;
    }

    public CompletableFuture<TipoConstruccion> obtenerTipoConstruccionAsync(String tipoNombre) {
        return CompletableFuture.supplyAsync(() -> {
            return obtenerTipoConstruccion(tipoNombre);
        });
    }

    public CompletableFuture<OrdenConstruccion> procesarOrdenAsync(OrdenRequest request, String jobId) {
       
        return CompletableFuture.supplyAsync(() -> {
            // Ejecución de la lógica de negocio de forma no bloqueante

            TipoConstruccion tipo = obtenerTipoConstruccion(request.getTipo());
            Coordenada coordenada = new Coordenada(request.getX(), request.getY());
            validarCoordenada(coordenada);
            inventarioService.consumirMateriales(tipo.getRequerimientos());

            OrdenConstruccion orden = crearOrden(tipo, coordenada);
            System.out.println("✅ Orden " + jobId + " completada por el Servicio");
            return orden;
        }, orderProcessingExecutor);

    }

    /**
     * Ideal para transformaciones lentas, pesadas o potencialmente bloqueantes,
     * ya que no bloquea el hilo del future que acaba de completarse y utiliza
     * eficientemente
     * los recursos del sistema de hilos.
     * Sin executor (default)
     * Puede fallar en capturar algunas excepciones sincrónicas
     */
    public CompletableFuture<OrdenConstruccion> procesarOrdenAsyncV2(OrdenRequest request, String jobId) {
        Coordenada coordenada = new Coordenada(request.getX(), request.getY());

        return obtenerTipoConstruccionAsync(request.getTipo())
                .thenComposeAsync(tipo -> validarCoordenadaAsync(coordenada) // Validación asíncrona
                        .thenApply(v -> tipo), // Si pasa, se devuelve el tipo
                        orderProcessingExecutor)
                .thenComposeAsync(tipo -> inventarioService.consumirMaterialesAsync(tipo.getRequerimientos())
                        .thenApply(v -> tipo),
                        orderProcessingExecutor)
                .thenApplyAsync(tipo -> crearOrden(tipo, coordenada), orderProcessingExecutor)
                .handleAsync((orden, ex) -> {
                    if (ex != null) {
                        throw new CompletionException(ex.getCause());
                    }
                    return orden;
                }, orderProcessingExecutor);
    }

    /**
     * Programa la siguiente orden solo si no hay otra en progreso.
     * La fecha de inicio es un día después de la última orden finalizada.
     */
    public CompletableFuture<Optional<OrdenConstruccion>> programarSiguienteOrdenAsync() {
        return CompletableFuture.supplyAsync(() -> {

            if (hayOrdenEnProgreso()) {
                System.out.println("⚠️ Ya existe una orden en progreso. No se puede programar otra.");
                return Optional.empty();
            }

            Optional<OrdenConstruccion> ultimaFinalizada = obtenerUltimaFinalizada();

            return programarSiguientePendienteSegun(ultimaFinalizada);

        }, orderProcessingExecutor);
    }
    // ---------------------------------------------------
    // Métodos auxiliares privados
    // ---------------------------------------------------

    private boolean hayOrdenEnProgreso() {
        return ordenRepository.existsByEstado(EstadoOrden.EN_PROGRESO);
    }

    private Optional<OrdenConstruccion> obtenerUltimaFinalizada() {
        return ordenRepository.findFirstByEstadoOrderByFechaSolicitudDesc(EstadoOrden.FINALIZADO);
    }

    private Optional<OrdenConstruccion> programarSiguientePendienteSegun(Optional<OrdenConstruccion> ultimaFinalizada) {
        return ordenRepository.findFirstByEstadoOrderByFechaSolicitudAsc(EstadoOrden.PENDIENTE)
                .map(pendiente -> {
                    LocalDate inicio = ultimaFinalizada
                            .map(orden -> orden.getFechaFinProgramada().plusDays(1))
                            .orElse(pendiente.getFechaSolicitud().plusDays(1));

                    return programarOrden(pendiente, inicio);
                })
                .or(() -> {
                    System.out.println("⚠️ No hay órdenes pendientes.");
                    return Optional.empty();
                });
    }

    private OrdenConstruccion programarOrden(OrdenConstruccion orden, LocalDate inicio) {
        LocalDate fin = inicio.plusDays(orden.getTipo().getDuracionDias() + 1);
        orden.setFechas(inicio, fin);
        orden.setEstado(EstadoOrden.EN_PROGRESO);
        ordenRepository.save(orden);
        System.out.println("✅ Orden programada: " + orden.getId());
        return orden;
    }

    /**
     * Marca la orden en progreso como finalizada, si la fecha fin es hoy.
     */
    public CompletableFuture<Optional<OrdenConstruccion>> finalizarOrdenSiCorrespondeAsync() {
        return CompletableFuture.supplyAsync(() -> obtenerOrdenEnProgreso()
                .flatMap(this::finalizarSiCorresponde)
                .or(() -> {
                    System.out.println("ℹ️ No hay órdenes en progreso para finalizar.");
                    return Optional.empty();
                }), orderProcessingExecutor);
    }

    // ---------------------------------------------------
    // Métodos auxiliares privados
    // -
    private Optional<OrdenConstruccion> obtenerOrdenEnProgreso() {
        // Usa directamente la consulta sin necesidad de un if
        return ordenRepository.findFirstByEstadoOrderByFechaSolicitudAsc(EstadoOrden.EN_PROGRESO);
    }

    private Optional<OrdenConstruccion> finalizarSiCorresponde(OrdenConstruccion orden) {
        LocalDate hoy = LocalDate.now();

        return Optional.ofNullable(orden.getFechaFinProgramada())
                .filter(fechaFin -> fechaFin.isEqual(hoy))
                .map(fechaFin -> finalizarOrden(orden))
                .or(() -> {
                    System.out.println("La orden aun no finaliza.");
                    return Optional.empty();
                });
    }

    private OrdenConstruccion finalizarOrden(OrdenConstruccion orden) {
        orden.setEstado(EstadoOrden.FINALIZADO);
        ordenRepository.save(orden);
        System.out.println("✅ Orden finalizada: " + orden.getId());
        return orden;
    }
}
