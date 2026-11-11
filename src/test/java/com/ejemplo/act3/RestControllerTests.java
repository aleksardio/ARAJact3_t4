package com.ejemplo.act3;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.ejemplo.act3.controller.CategoriaRestController;
import com.ejemplo.act3.controller.ManejadorErroresRest;
import com.ejemplo.act3.controller.MateriaRestController;
import com.ejemplo.act3.service.CategoriaService;
import com.ejemplo.act3.service.CategoriaService.DatosCategoria;
import com.ejemplo.act3.service.ConflictoException;
import com.ejemplo.act3.service.MateriaService;
import com.ejemplo.act3.service.MateriaService.DatosMateria;

class RestControllerTests {
    private CategoriaService categorias;
    private MateriaService materias;
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        categorias = mock(CategoriaService.class);
        materias = mock(MateriaService.class);
        mvc = MockMvcBuilders.standaloneSetup(new CategoriaRestController(categorias),
                new MateriaRestController(materias)).setControllerAdvice(new ManejadorErroresRest()).build();
    }

    @Test
    void creaCategoriaConLocation() throws Exception {
        when(categorias.crear("Ciencias", "Naturales"))
                .thenReturn(new DatosCategoria(1L, "Ciencias", "Naturales"));
        mvc.perform(post("/api/categorias").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Ciencias\",\"descripcion\":\"Naturales\"}"))
                .andExpect(status().isCreated()).andExpect(header().string("Location", "/api/categorias/1"))
                .andExpect(jsonPath("$.nombre").value("Ciencias"));
    }

    @Test
    void materiaIncluyeCategoriaYFiltro() throws Exception {
        DatosMateria dato = new DatosMateria(2L, "Física", "Ana", 2, 1L, "Ciencias");
        when(materias.listarPorCategoria(1L)).thenReturn(List.of(dato));
        when(materias.crear("Física", "Ana", 2, 1L)).thenReturn(dato);
        mvc.perform(get("/api/materias").param("categoriaId", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].categoriaNombre").value("Ciencias"));
        mvc.perform(post("/api/materias").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Física\",\"docente\":\"Ana\",\"semestre\":2,\"categoriaId\":1}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.categoriaNombre").value("Ciencias"));
    }

    @Test
    void validaEntradaYFormato() throws Exception {
        mvc.perform(post("/api/materias").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"X\",\"docente\":\"Ana\",\"semestre\":13,\"categoriaId\":1}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/materias").param("categoriaId", "abc"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/categorias").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void diferenciaNoEncontradoYConflicto() throws Exception {
        when(categorias.buscarPorId(99L)).thenThrow(new NoSuchElementException("No existe la categoría"));
        when(categorias.crear(eq("Ciencias"), any())).thenThrow(new ConflictoException("Categoría duplicada"));
        doThrow(new ConflictoException("Tiene materias")).when(categorias).eliminar(1L);
        mvc.perform(get("/api/categorias/99")).andExpect(status().isNotFound());
        mvc.perform(post("/api/categorias").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Ciencias\",\"descripcion\":\"X\"}"))
                .andExpect(status().isConflict());
        mvc.perform(delete("/api/categorias/1")).andExpect(status().isConflict());
    }

    @Test
    void actualizaYElimina() throws Exception {
        when(categorias.actualizar(1L, "Nueva", null)).thenReturn(new DatosCategoria(1L, "Nueva", null));
        mvc.perform(put("/api/categorias/1").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Nueva\"}")).andExpect(status().isOk());
        mvc.perform(delete("/api/materias/2")).andExpect(status().isNoContent());
    }
}
