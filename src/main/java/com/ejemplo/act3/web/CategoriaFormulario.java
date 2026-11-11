package com.ejemplo.act3.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CategoriaFormulario {
    @NotBlank(message = "Escribe el nombre de la categoría")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    private String nombre;
    @Size(max = 255, message = "La descripción no puede superar 255 caracteres")
    private String descripcion;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
