package com.ejemplo.act3.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ejemplo.act3.service.MateriaService;
import com.ejemplo.act3.service.MateriaService.DatosMateria;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/materias")
public class MateriaRestController {
    private final MateriaService servicio;

    public MateriaRestController(MateriaService servicio) {
        this.servicio = servicio;
    }

    public record SolicitudMateria(@NotBlank @Size(max = 120) String nombre,
            @NotBlank @Size(max = 120) String docente,
            @NotNull @Min(1) @Max(12) Integer semestre,
            @NotNull Long categoriaId) {
    }

    @GetMapping
    public List<DatosMateria> listar(@RequestParam(required = false) Long categoriaId) {
        return categoriaId == null ? servicio.listar() : servicio.listarPorCategoria(categoriaId);
    }

    @GetMapping("/{id}")
    public DatosMateria buscar(@PathVariable Long id) {
        return servicio.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<DatosMateria> crear(@Valid @RequestBody SolicitudMateria solicitud) {
        DatosMateria creada = servicio.crear(solicitud.nombre(), solicitud.docente(),
                solicitud.semestre(), solicitud.categoriaId());
        return ResponseEntity.created(URI.create("/api/materias/" + creada.id())).body(creada);
    }

    @PutMapping("/{id}")
    public DatosMateria actualizar(@PathVariable Long id, @Valid @RequestBody SolicitudMateria solicitud) {
        return servicio.actualizar(id, solicitud.nombre(), solicitud.docente(),
                solicitud.semestre(), solicitud.categoriaId());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
