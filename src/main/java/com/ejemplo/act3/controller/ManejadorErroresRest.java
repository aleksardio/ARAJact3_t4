package com.ejemplo.act3.controller;

import java.util.NoSuchElementException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.ejemplo.act3.service.ConflictoException;
import com.ejemplo.act3.service.DatosInvalidosException;

@RestControllerAdvice(assignableTypes = {CategoriaRestController.class, MateriaRestController.class})
public class ManejadorErroresRest {
    public record ErrorRest(int estado, String mensaje) {
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRest> validacion(MethodArgumentNotValidException excepcion) {
        String mensaje = excepcion.getBindingResult().getFieldErrors().stream().findFirst()
                .map(error -> "Campo " + error.getField() + ": " + error.getDefaultMessage())
                .orElse("Datos de entrada inválidos");
        return respuesta(HttpStatus.BAD_REQUEST, mensaje);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorRest> entradaInvalida(Exception excepcion) {
        return respuesta(HttpStatus.BAD_REQUEST, "El formato de la solicitud no es válido");
    }

    @ExceptionHandler(DatosInvalidosException.class)
    public ResponseEntity<ErrorRest> argumentoInvalido(DatosInvalidosException excepcion) {
        return respuesta(HttpStatus.BAD_REQUEST, excepcion.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorRest> noEncontrado(NoSuchElementException excepcion) {
        return respuesta(HttpStatus.NOT_FOUND, excepcion.getMessage());
    }

    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<ErrorRest> conflicto(ConflictoException excepcion) {
        return respuesta(HttpStatus.CONFLICT, excepcion.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorRest> integridad(DataIntegrityViolationException excepcion) {
        return respuesta(HttpStatus.CONFLICT, "La operación entra en conflicto con los datos existentes");
    }

    private ResponseEntity<ErrorRest> respuesta(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(new ErrorRest(estado.value(), mensaje));
    }
}
