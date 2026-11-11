package com.ejemplo.act3.web;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ejemplo.act3.service.CategoriaService;
import com.ejemplo.act3.service.DatosInvalidosException;
import com.ejemplo.act3.service.MateriaService;
import com.ejemplo.act3.service.MateriaService.DatosMateria;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/materias")
public class MateriaController {
    private final MateriaService materias;
    private final CategoriaService categorias;

    public MateriaController(MateriaService materias, CategoriaService categorias) {
        this.materias = materias;
        this.categorias = categorias;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Integer semestre, Model modelo) {
        List<DatosMateria> lista = materias.listar().stream()
                .filter(m -> categoriaId == null || categoriaId.equals(m.categoriaId()))
                .filter(m -> semestre == null || semestre.equals(m.semestre())).toList();
        modelo.addAttribute("materias", lista);
        modelo.addAttribute("categorias", categorias.listar());
        modelo.addAttribute("categoriaId", categoriaId);
        modelo.addAttribute("semestre", semestre);
        return "materias/lista";
    }

    @GetMapping("/nueva")
    public String nueva(Model modelo) {
        modelo.addAttribute("formulario", new MateriaFormulario());
        return formulario(modelo, null);
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model modelo) {
        modelo.addAttribute("materia", materias.buscarPorId(id));
        return "materias/detalle";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model modelo) {
        DatosMateria materia = materias.buscarPorId(id);
        MateriaFormulario datos = new MateriaFormulario();
        datos.setNombre(materia.nombre());
        datos.setDocente(materia.docente());
        datos.setSemestre(materia.semestre());
        datos.setCategoriaId(materia.categoriaId());
        modelo.addAttribute("formulario", datos);
        return formulario(modelo, id);
    }

    @PostMapping({"/nueva", "/{id}/editar"})
    public String guardar(@PathVariable(required = false) Long id,
            @Valid @ModelAttribute("formulario") MateriaFormulario datos, BindingResult errores,
            Model modelo, RedirectAttributes redireccion) {
        if (!errores.hasErrors()) {
            try {
                if (id == null) {
                    materias.crear(datos.getNombre(), datos.getDocente(), datos.getSemestre(), datos.getCategoriaId());
                } else {
                    materias.actualizar(id, datos.getNombre(), datos.getDocente(), datos.getSemestre(), datos.getCategoriaId());
                }
                redireccion.addFlashAttribute("exito", id == null ? "Materia registrada correctamente." : "Materia actualizada correctamente.");
                return "redirect:/materias";
            } catch (NoSuchElementException ex) {
                // La categoría seleccionada o la materia editada pudo desaparecer.
                errores.reject("noExiste", "La materia o categoría seleccionada ya no existe. Revisa los datos.");
            } catch (DatosInvalidosException ex) {
                errores.reject("invalido", ex.getMessage());
            } catch (DataIntegrityViolationException ex) {
                errores.reject("integridad", "No se pudo guardar: los datos relacionados cambiaron. Inténtalo de nuevo.");
            }
        }
        return formulario(modelo, id);
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes redireccion) {
        try {
            materias.eliminar(id);
            redireccion.addFlashAttribute("exito", "Materia eliminada correctamente.");
        } catch (NoSuchElementException ex) {
            redireccion.addFlashAttribute("error", "La materia ya no existe.");
        } catch (DataIntegrityViolationException ex) {
            redireccion.addFlashAttribute("error", "No se pudo eliminar la materia por un conflicto con otros datos.");
        }
        return "redirect:/materias";
    }

    private String formulario(Model modelo, Long id) {
        modelo.addAttribute("categorias", categorias.listar());
        modelo.addAttribute("id", id);
        return "materias/formulario";
    }
}
