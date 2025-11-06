package com.semillero.Constructores.configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*Se utiliza porque lógica de negocio tiene muchas operaciones 
I/O bloqueantes (ej. llamadas a bases de datos o servicios externos), 
estas bloquearán los hilos del  common pool, degradando el rendimiento
 de toda la aplicación */
@Configuration
public class AsyncConfig {
    @Bean(destroyMethod = "shutdown")
    public ExecutorService orderProcessingExecutor() {
        // Crear un pool de hilos dedicado, optimizado para I/O (más hilos, pero
        // ligeros)
        // El número óptimo depende de tu carga de trabajo.
        return Executors.newFixedThreadPool(10);
    }

}
