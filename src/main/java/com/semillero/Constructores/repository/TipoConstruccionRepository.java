package com.semillero.Constructores.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.semillero.Constructores.domain.TipoConstruccion;

public interface TipoConstruccionRepository extends MongoRepository<TipoConstruccion, String>{
     Optional<TipoConstruccion> findByNombreIgnoreCase(String nombre);
}
