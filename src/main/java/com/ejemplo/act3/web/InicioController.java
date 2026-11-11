package com.ejemplo.act3.web;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.ejemplo.act3.service.CategoriaService;
import com.ejemplo.act3.service.MateriaService;
import com.ejemplo.act3.service.MateriaService.DatosMateria;

@Controller
public class InicioController {
    private final CategoriaService categorias;
    private final MateriaService materias;

    public InicioController(CategoriaService categorias, MateriaService materias) {
        this.categorias = categorias;
        this.materias = materias;
    }

    @GetMapping("/")
    public String inicio(Model modelo) {
        List<DatosMateria> lista = materias.listar();
        modelo.addAttribute("totalMaterias", lista.size());
        modelo.addAttribute("totalCategorias", categorias.listar().size());
        modelo.addAttribute("materiasSemestre8", lista.stream().filter(m -> m.semestre() == 8).count());
        return "inicio";
    }
}
