package com.ejemplo.act3.web;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class MateriaFormulario {
    @NotBlank(message = "Escribe el nombre de la materia")
    @Size(max = 120, message = "El nombre no puede superar 120 caracteres")
    private String nombre;
    @NotBlank(message = "Escribe el nombre del docente")
    @Size(max = 120, message = "El docente no puede superar 120 caracteres")
    private String docente;
    @NotNull(message = "Indica el semestre")
    @Min(value = 1, message = "El semestre debe estar entre 1 y 12")
    @Max(value = 12, message = "El semestre debe estar entre 1 y 12")
    private Integer semestre;
    @NotNull(message = "Selecciona una categoría")
    private Long categoriaId;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDocente() { return docente; }
    public void setDocente(String docente) { this.docente = docente; }
    public Integer getSemestre() { return semestre; }
    public void setSemestre(Integer semestre) { this.semestre = semestre; }
    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }
}
