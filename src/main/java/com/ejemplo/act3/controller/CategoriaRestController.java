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
import org.springframework.web.bind.annotation.RestController;

import com.ejemplo.act3.service.CategoriaService;
import com.ejemplo.act3.service.CategoriaService.DatosCategoria;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaRestController {
    private final CategoriaService servicio;

    public CategoriaRestController(CategoriaService servicio) {
        this.servicio = servicio;
    }

    public record SolicitudCategoria(@NotBlank @Size(max = 100) String nombre,
            @Size(max = 255) String descripcion) {
    }

    @GetMapping
    public List<DatosCategoria> listar() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    public DatosCategoria buscar(@PathVariable Long id) {
        return servicio.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<DatosCategoria> crear(@Valid @RequestBody SolicitudCategoria solicitud) {
        DatosCategoria creada = servicio.crear(solicitud.nombre(), solicitud.descripcion());
        return ResponseEntity.created(URI.create("/api/categorias/" + creada.id())).body(creada);
    }

    @PutMapping("/{id}")
    public DatosCategoria actualizar(@PathVariable Long id, @Valid @RequestBody SolicitudCategoria solicitud) {
        return servicio.actualizar(id, solicitud.nombre(), solicitud.descripcion());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
