package com.example.demo.controller;

import com.example.demo.config.InvalidContentTypeException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(InvalidContentTypeException.class)
    public ResponseEntity<String> handleInvalidContentTypeException(InvalidContentTypeException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
 
}