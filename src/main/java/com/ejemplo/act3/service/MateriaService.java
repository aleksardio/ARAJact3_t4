package com.ejemplo.act3.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ejemplo.act3.model.Categoria;
import com.ejemplo.act3.model.Materia;
import com.ejemplo.act3.repository.CategoriaRepository;
import com.ejemplo.act3.repository.MateriaRepository;

@Service
@Transactional(readOnly = true)
public class MateriaService {
    private final MateriaRepository materias;
    private final CategoriaRepository categorias;

    public MateriaService(MateriaRepository materias, CategoriaRepository categorias) {
        this.materias = materias;
        this.categorias = categorias;
    }

    public record DatosMateria(Long id, String nombre, String docente, Integer semestre,
            Long categoriaId, String categoriaNombre) {
    }

    public List<DatosMateria> listar() {
        return materias.findAll().stream().map(MateriaService::datos).toList();
    }

    public List<DatosMateria> listarPorCategoria(Long categoriaId) {
        obtenerCategoria(categoriaId);
        return materias.findByCategoriaId(categoriaId).stream().map(MateriaService::datos).toList();
    }

    public DatosMateria buscarPorId(Long id) {
        return datos(obtener(id));
    }

    @Transactional
    public DatosMateria crear(String nombre, String docente, Integer semestre, Long categoriaId) {
        Materia materia = new Materia();
        asignarDatos(materia, nombre, docente, semestre, categoriaId);
        return datos(materias.save(materia));
    }

    @Transactional
    public DatosMateria actualizar(Long id, String nombre, String docente, Integer semestre, Long categoriaId) {
        Materia materia = obtener(id);
        asignarDatos(materia, nombre, docente, semestre, categoriaId);
        return datos(materias.save(materia));
    }

    @Transactional
    public void eliminar(Long id) {
        materias.delete(obtener(id));
    }

    private void asignarDatos(Materia materia, String nombre, String docente, Integer semestre, Long categoriaId) {
        String nombreValidado = validarTexto(nombre, "nombre");
        String docenteValidado = validarTexto(docente, "docente");
        if (semestre == null || semestre < 1 || semestre > 12) {
            throw new IllegalArgumentException("El semestre debe estar entre 1 y 12");
        }
        Categoria categoria = obtenerCategoria(categoriaId);
        materia.setNombre(nombreValidado);
        materia.setDocente(docenteValidado);
        materia.setSemestre(semestre);
        materia.setCategoria(categoria);
    }

    private Materia obtener(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El identificador de materia es obligatorio");
        }
        return materias.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No existe la materia con id " + id));
    }

    private Categoria obtenerCategoria(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El identificador de categoría es obligatorio");
        }
        return categorias.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No existe la categoría con id " + id));
    }

    private static String validarTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo " + campo + " es obligatorio");
        }
        String resultado = valor.strip();
        if (resultado.length() > 120) {
            throw new IllegalArgumentException("El campo " + campo + " no puede superar 120 caracteres");
        }
        return resultado;
    }

    private static DatosMateria datos(Materia materia) {
        return new DatosMateria(materia.getId(), materia.getNombre(), materia.getDocente(),
                materia.getSemestre(), materia.getCategoria().getId(), materia.getCategoria().getNombre());
    }
}
