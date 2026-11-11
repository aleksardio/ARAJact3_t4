package com.ejemplo.act3.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ejemplo.act3.model.Categoria;
import com.ejemplo.act3.repository.CategoriaRepository;
import com.ejemplo.act3.repository.MateriaRepository;

@Service
@Transactional(readOnly = true)
public class CategoriaService {
    private final CategoriaRepository categorias;
    private final MateriaRepository materias;

    public CategoriaService(CategoriaRepository categorias, MateriaRepository materias) {
        this.categorias = categorias;
        this.materias = materias;
    }

    public record DatosCategoria(Long id, String nombre, String descripcion) {
    }

    public List<DatosCategoria> listar() {
        return categorias.findAll().stream().map(CategoriaService::datos).toList();
    }

    public DatosCategoria buscarPorId(Long id) {
        return datos(obtener(id));
    }

    @Transactional
    public DatosCategoria crear(String nombre, String descripcion) {
        String nombreValidado = validarNombre(nombre);
        validarDescripcion(descripcion);
        if (categorias.existsByNombre(nombreValidado)) {
            throw new ConflictoException("Ya existe una categoría con ese nombre");
        }
        Categoria categoria = new Categoria();
        categoria.setNombre(nombreValidado);
        categoria.setDescripcion(descripcion);
        return datos(categorias.save(categoria));
    }

    @Transactional
    public DatosCategoria actualizar(Long id, String nombre, String descripcion) {
        Categoria categoria = obtener(id);
        String nombreValidado = validarNombre(nombre);
        validarDescripcion(descripcion);
        categorias.findByNombre(nombreValidado).ifPresent(otra -> {
            if (!otra.getId().equals(id)) {
                throw new ConflictoException("Ya existe una categoría con ese nombre");
            }
        });
        categoria.setNombre(nombreValidado);
        categoria.setDescripcion(descripcion);
        return datos(categorias.save(categoria));
    }

    @Transactional
    public void eliminar(Long id) {
        Categoria categoria = obtener(id);
        if (materias.existsByCategoriaId(id)) {
            throw new ConflictoException("No se puede eliminar una categoría con materias asociadas");
        }
        categorias.delete(categoria);
    }

    private Categoria obtener(Long id) {
        if (id == null) {
            throw new DatosInvalidosException("El identificador de categoría es obligatorio");
        }
        return categorias.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No existe la categoría con id " + id));
    }

    private static String validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new DatosInvalidosException("El nombre de la categoría es obligatorio");
        }
        String resultado = nombre.strip();
        if (resultado.length() > 100) {
            throw new DatosInvalidosException("El nombre de la categoría no puede superar 100 caracteres");
        }
        return resultado;
    }

    private static void validarDescripcion(String descripcion) {
        if (descripcion != null && descripcion.length() > 255) {
            throw new DatosInvalidosException("La descripción no puede superar 255 caracteres");
        }
    }

    private static DatosCategoria datos(Categoria categoria) {
        return new DatosCategoria(categoria.getId(), categoria.getNombre(), categoria.getDescripcion());
    }
}
