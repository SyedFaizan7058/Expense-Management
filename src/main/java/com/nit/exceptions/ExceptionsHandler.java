package com.nit.exceptions;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.resource.NoResourceFoundException;

//Global exception handler
@ControllerAdvice
public class ExceptionsHandler {

    @ExceptionHandler(NoResourceFoundException.class)
    public String handleException(NoResourceFoundException ex) {
        return "/404";
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public String allExceptions(Exception ex) {
        return "An error has occurred, please try again";
    }

}
