package com.semillero.Constructores.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.semillero.Constructores.domain.OrdenConstruccion;
import com.semillero.Constructores.domain.model.EstadoOrden;
import com.semillero.Constructores.domain.model.EstadoOrdenTotal;

public interface OrdenConstruccionRepository extends MongoRepository<OrdenConstruccion, String> {
    // Ejemplos de consultas personalizadas
    List<OrdenConstruccion> findByEstado(EstadoOrden estado);

    Optional<OrdenConstruccion> findFirstByEstadoOrderByFechaSolicitudAsc(EstadoOrden estado);
    Optional<OrdenConstruccion> findFirstByEstadoOrderByFechaSolicitudDesc(EstadoOrden estado);
    Optional<OrdenConstruccion> findFirstByOrderByFechaSolicitudAsc();
     boolean existsByEstado(EstadoOrden estado);
       @Aggregation(pipeline = {
        "{ $group: { _id: '$estado', total: { $sum: 1 } } }",
        "{ $project: { _id: 0, estado: '$_id', total: 1 } }"
    })
    List<EstadoOrdenTotal> contarPorEstado();

}
