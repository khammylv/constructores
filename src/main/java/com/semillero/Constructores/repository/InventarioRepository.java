package com.semillero.Constructores.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.semillero.Constructores.domain.Inventario;

@Repository
public interface InventarioRepository extends MongoRepository<Inventario, String>  {

}
