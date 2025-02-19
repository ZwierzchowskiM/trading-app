package com.mzwierzchowski.trading_app.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(value = UserNotFoundException.class)
  public ResponseEntity<Object> exception(UserNotFoundException exception) {
    String username = exception.getMessage();
    return new ResponseEntity<>("User " + username +" not found", HttpStatus.NOT_FOUND);
  }
  }
