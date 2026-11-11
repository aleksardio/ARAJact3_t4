package com.ejemplo.act3;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;

import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.ejemplo.act3.service.CategoriaService;
import com.ejemplo.act3.service.CategoriaService.DatosCategoria;
import com.ejemplo.act3.service.ConflictoException;
import com.ejemplo.act3.service.MateriaService;
import com.ejemplo.act3.service.MateriaService.DatosMateria;
import com.ejemplo.act3.controller.CategoriaRestController;
import com.ejemplo.act3.controller.MateriaRestController;
import com.ejemplo.act3.web.CategoriaController;
import com.ejemplo.act3.web.ErroresMvc;
import com.ejemplo.act3.web.InicioController;
import com.ejemplo.act3.web.MateriaController;

class MvcControllerTests {
    private CategoriaService categorias;
    private MateriaService materias;
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        categorias = mock(CategoriaService.class);
        materias = mock(MateriaService.class);
        when(categorias.listar()).thenReturn(List.of(new DatosCategoria(1L, "Programación", "Área")));
        when(materias.listar()).thenReturn(List.of(new DatosMateria(2L, "Programación Web", "Por asignar", 8, 1L, "Programación")));
        ClassLoaderTemplateResolver plantillas = new ClassLoaderTemplateResolver();
        plantillas.setPrefix("templates/");
        plantillas.setSuffix(".html");
        plantillas.setTemplateMode("HTML");
        plantillas.setCharacterEncoding("UTF-8");
        SpringTemplateEngine motor = new SpringTemplateEngine();
        motor.setTemplateResolver(plantillas);
        ThymeleafViewResolver vistas = new ThymeleafViewResolver();
        vistas.setTemplateEngine(motor);
        vistas.setCharacterEncoding("UTF-8");
        mvc = MockMvcBuilders.standaloneSetup(new InicioController(categorias, materias),
                new MateriaController(materias, categorias), new CategoriaController(categorias, materias),
                new CategoriaRestController(categorias), new MateriaRestController(materias))
                .setControllerAdvice(new ErroresMvc()).setViewResolvers(vistas).build();
    }

    @Test
    void renderizaDashboardListasYFiltros() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk()).andExpect(content().string(containsString("Tu resumen académico")));
        mvc.perform(get("/materias").param("categoriaId", "1").param("semestre", "8"))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Programación Web")));
        mvc.perform(get("/materias").param("semestre", "7"))
                .andExpect(content().string(containsString("No se encontraron materias")));
        mvc.perform(get("/categorias")).andExpect(status().isOk())
                .andExpect(content().string(containsString("No se puede eliminar: contiene materias")));
        mvc.perform(get("/api/materias")).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"));
        mvc.perform(get("/api/categorias")).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"));
    }

    @Test
    void formulariosMantienenDatosYRenderizanErrores() throws Exception {
        mvc.perform(get("/materias/nueva")).andExpect(status().isOk()).andExpect(content().string(containsString("Selecciona una categoría")));
        mvc.perform(post("/materias/nueva").param("nombre", "Materia de prueba").param("docente", "")
                .param("semestre", "13").param("categoriaId", "1"))
                .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("formulario", "docente", "semestre"))
                .andExpect(content().string(containsString("Materia de prueba")));
        verify(materias, never()).crear(any(), any(), any(), any());
        mvc.perform(get("/categorias/nueva")).andExpect(status().isOk());
        mvc.perform(post("/categorias/nueva").param("nombre", " ").param("descripcion", "Conservar"))
                .andExpect(model().attributeHasFieldErrors("formulario", "nombre"))
                .andExpect(content().string(containsString("Conservar")));
    }

    @Test
    void editaConsultaYEliminaMediantePost() throws Exception {
        DatosMateria materia = new DatosMateria(2L, "Programación Web", "Por asignar", 8, 1L, "Programación");
        when(materias.buscarPorId(2L)).thenReturn(materia);
        when(categorias.buscarPorId(1L)).thenReturn(new DatosCategoria(1L, "Programación", "Área"));
        mvc.perform(get("/materias/2")).andExpect(status().isOk()).andExpect(content().string(containsString("Por asignar")));
        mvc.perform(get("/materias/2/editar")).andExpect(status().isOk()).andExpect(content().string(containsString("Por asignar")));
        mvc.perform(get("/categorias/1/editar")).andExpect(status().isOk());
        mvc.perform(post("/materias/2/editar").param("nombre", "Programación Web").param("docente", "Ana")
                .param("semestre", "8").param("categoriaId", "1"))
                .andExpect(redirectedUrl("/materias"));
        verify(materias).actualizar(2L, "Programación Web", "Ana", 8, 1L);
        mvc.perform(post("/materias/2/eliminar")).andExpect(redirectedUrl("/materias"))
                .andExpect(flash().attributeExists("exito"));
        mvc.perform(get("/materias/2/eliminar")).andExpect(status().isMethodNotAllowed());
    }

    @Test
    void informaConflictoYRegistroInexistente() throws Exception {
        doThrow(new ConflictoException("La categoría tiene materias")).when(categorias).eliminar(1L);
        mvc.perform(post("/categorias/1/eliminar")).andExpect(redirectedUrl("/categorias"))
                .andExpect(flash().attributeExists("error"));
        when(materias.buscarPorId(99L)).thenThrow(new NoSuchElementException());
        mvc.perform(get("/materias/99")).andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Registro no encontrado")));
    }
}
