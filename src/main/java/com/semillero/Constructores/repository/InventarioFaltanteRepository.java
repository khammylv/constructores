package com.semillero.Constructores.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.semillero.Constructores.domain.model.InventarioFaltante;

public interface InventarioFaltanteRepository extends MongoRepository<InventarioFaltante, String> {

}
