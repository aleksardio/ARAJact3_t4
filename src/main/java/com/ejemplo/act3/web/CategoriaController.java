package com.ejemplo.act3.web;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ejemplo.act3.service.CategoriaService;
import com.ejemplo.act3.service.CategoriaService.DatosCategoria;
import com.ejemplo.act3.service.ConflictoException;
import com.ejemplo.act3.service.DatosInvalidosException;
import com.ejemplo.act3.service.MateriaService;
import com.ejemplo.act3.service.MateriaService.DatosMateria;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/categorias")
public class CategoriaController {
    private final CategoriaService categorias;
    private final MateriaService materias;

    public CategoriaController(CategoriaService categorias, MateriaService materias) {
        this.categorias = categorias;
        this.materias = materias;
    }

    @GetMapping
    public String listar(Model modelo) {
        Map<Long, Long> cantidades = materias.listar().stream()
                .collect(Collectors.groupingBy(DatosMateria::categoriaId, Collectors.counting()));
        modelo.addAttribute("categorias", categorias.listar());
        modelo.addAttribute("cantidades", cantidades);
        return "categorias/lista";
    }

    @GetMapping("/nueva")
    public String nueva(Model modelo) {
        modelo.addAttribute("formulario", new CategoriaFormulario());
        return formulario(modelo, null);
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model modelo) {
        DatosCategoria categoria = categorias.buscarPorId(id);
        CategoriaFormulario datos = new CategoriaFormulario();
        datos.setNombre(categoria.nombre());
        datos.setDescripcion(categoria.descripcion());
        modelo.addAttribute("formulario", datos);
        return formulario(modelo, id);
    }

    @PostMapping({"/nueva", "/{id}/editar"})
    public String guardar(@PathVariable(required = false) Long id,
            @Valid @ModelAttribute("formulario") CategoriaFormulario datos, BindingResult errores,
            Model modelo, RedirectAttributes redireccion) {
        if (!errores.hasErrors()) {
            try {
                if (id == null) {
                    categorias.crear(datos.getNombre(), datos.getDescripcion());
                } else {
                    categorias.actualizar(id, datos.getNombre(), datos.getDescripcion());
                }
                redireccion.addFlashAttribute("exito", id == null ? "Categoría registrada correctamente." : "Categoría actualizada correctamente.");
                return "redirect:/categorias";
            } catch (ConflictoException | DatosInvalidosException ex) {
                errores.reject("negocio", ex.getMessage());
            } catch (DataIntegrityViolationException ex) {
                errores.reject("integridad", "No se pudo guardar la categoría por un conflicto con datos existentes.");
            } catch (NoSuchElementException ex) {
                errores.reject("noExiste", "La categoría ya no existe.");
            }
        }
        return formulario(modelo, id);
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes redireccion) {
        try {
            categorias.eliminar(id);
            redireccion.addFlashAttribute("exito", "Categoría eliminada correctamente.");
        } catch (ConflictoException ex) {
            redireccion.addFlashAttribute("error", ex.getMessage());
        } catch (NoSuchElementException ex) {
            redireccion.addFlashAttribute("error", "La categoría ya no existe.");
        } catch (DataIntegrityViolationException ex) {
            redireccion.addFlashAttribute("error", "No se puede eliminar la categoría porque está asociada a otros datos.");
        }
        return "redirect:/categorias";
    }

    private String formulario(Model modelo, Long id) {
        modelo.addAttribute("id", id);
        return "categorias/formulario";
    }
}
