package com.ejemplo.act3.web;

import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice(assignableTypes = {InicioController.class, MateriaController.class, CategoriaController.class})
public class ErroresMvc {
    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String noEncontrado(Model modelo) {
        modelo.addAttribute("mensaje", "El registro que buscas no existe o fue eliminado.");
        return "no-encontrado";
    }
}
