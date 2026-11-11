package com.ejemplo.act3.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "categorias")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "nombre", nullable = false, length = 100, unique = true)
    private String nombre;

    @Size(max = 255)
    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @OneToMany(mappedBy = "categoria")
    private List<Materia> materias = new ArrayList<>();

    public Categoria() {
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public List<Materia> getMaterias() {
        return Collections.unmodifiableList(materias);
    }

    void agregarMateria(Materia materia) {
        if (!materias.contains(materia)) {
            materias.add(materia);
        }
    }

    void quitarMateria(Materia materia) {
        materias.remove(materia);
    }
}
