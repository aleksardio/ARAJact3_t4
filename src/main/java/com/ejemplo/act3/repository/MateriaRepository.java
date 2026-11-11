package com.ejemplo.act3.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ejemplo.act3.model.Materia;

public interface MateriaRepository extends JpaRepository<Materia, Long> {
    List<Materia> findByCategoriaId(Long categoriaId);

    boolean existsByCategoriaId(Long categoriaId);
}
