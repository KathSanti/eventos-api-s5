package com.duoc.mscompras.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.duoc.mscompras.model.Evento;

public interface EventoRepository extends JpaRepository<Evento, Long> {

    Optional<Evento> findByNombreIgnoreCase(String nombre);
}
