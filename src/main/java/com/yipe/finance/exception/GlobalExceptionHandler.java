package com.yipe.finance.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ModelAndView handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return errorView(404, ex.getMessage(), request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ModelAndView handleNoResource(NoResourceFoundException ex, HttpServletRequest request) {
        return errorView(404, "Página não encontrada.", request);
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error processing {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return errorView(500, "Ocorreu um erro inesperado.", request);
    }

    private ModelAndView errorView(int status, String message, HttpServletRequest request) {
        ModelAndView mv = new ModelAndView("error");
        mv.addObject("status", status);
        mv.addObject("message", message);
        mv.addObject("path", request.getRequestURI());
        mv.addObject("timestamp", LocalDateTime.now());
        return mv;
    }
}
