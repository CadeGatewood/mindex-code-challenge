package com.mindex.challenge.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.mindex.challenge.data.ErrorResponse;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@ControllerAdvice
public class GlobalAdviceController {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalAdviceController.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        LOG.error(e.getMessage(), e);

        ErrorResponse errorResponse = new ErrorResponse("One should centralize error handling, probably in a more granular way...", e.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.I_AM_A_TEAPOT);
    }

}
