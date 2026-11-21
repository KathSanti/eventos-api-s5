package com.duoc.mscompras.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.duoc.mscompras.model.Compra;

public interface CompraRepository extends JpaRepository<Compra, Long> {

    // Trae la compra y su evento en la misma consulta
    Optional<Compra> findConEventoById(Long id);
}
