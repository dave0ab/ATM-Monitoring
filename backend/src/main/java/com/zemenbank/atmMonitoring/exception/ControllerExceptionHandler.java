package com.zemenbank.atmMonitoring.exception;
import java.util.Date;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ControllerExceptionHandler {
    
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorMessage> resourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
    ErrorMessage message = new ErrorMessage(
        HttpStatus.NOT_FOUND.value(),
        new Date(),
        ex.getMessage(),
        request.getDescription(false));
    
    return new ResponseEntity<ErrorMessage>(message, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(CustomException.class)
  public ResponseEntity<ErrorMessage> customException(CustomException ex, WebRequest request) {
    ErrorMessage message = new ErrorMessage(
        HttpStatus.BAD_REQUEST.value(),
        new Date(),
        ex.getMessage(),
        request.getDescription(false));
    
    return new ResponseEntity<ErrorMessage>(message, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<ErrorMessage> forbiddenException(ForbiddenException ex, WebRequest request) {
    ErrorMessage message = new ErrorMessage(
        HttpStatus.FORBIDDEN.value(),
        new Date(),
        ex.getMessage(),
        request.getDescription(false));
    
    return new ResponseEntity<ErrorMessage>(message, HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(ExpiredJwtException.class)
  public ResponseEntity<ErrorMessage> handleExpiredJwtException(ExpiredJwtException ex, WebRequest request) {

      ErrorMessage message = new ErrorMessage(
        HttpStatus.UNAUTHORIZED.value(),
        new Date(),
        ex.getMessage(),
        request.getDescription(false));      
        return new ResponseEntity<ErrorMessage>(message,  HttpStatus.UNAUTHORIZED);
      }
      


  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<ErrorMessage> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
          WebRequest request) {
  
  
  ErrorMessage message = new ErrorMessage(
    HttpStatus.BAD_REQUEST.value(),
    new Date(),
    ex.getMessage(),
    request.getDescription(false));
    return new ResponseEntity<ErrorMessage>(message,  HttpStatus.BAD_REQUEST);

}

@ExceptionHandler(ConstraintViolationException.class)
public ResponseEntity<ErrorMessage> ConstraintViolationException(ConstraintViolationException ex,
        WebRequest request) {


ErrorMessage message = new ErrorMessage(
  HttpStatus.BAD_REQUEST.value(),
  new Date(),
  ex.getMessage(),
  request.getDescription(false));
  return new ResponseEntity<ErrorMessage>(message,  HttpStatus.BAD_REQUEST);

}

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(
          MethodArgumentNotValidException ex, WebRequest request) {
      
      Map<String, String> fieldErrors = new HashMap<>();
      String mainMessage = "Validation failed";
      
      // Extract field-specific errors
      ex.getBindingResult().getFieldErrors().forEach((error) -> {
          String fieldName = error.getField();
          String errorMessage = error.getDefaultMessage();
          fieldErrors.put(fieldName, errorMessage);
      });
      
      // Create a user-friendly summary message
      if (!fieldErrors.isEmpty()) {
          String fieldNames = String.join(", ", fieldErrors.keySet());
          mainMessage = "Please fix the following fields: " + fieldNames;
      }

      ValidationErrorResponse errorResponse = ValidationErrorResponse.builder()
              .timestamp(LocalDateTime.now())
              .status(HttpStatus.BAD_REQUEST.value())
              .error("Validation Failed")
              .message(mainMessage)
              .validationErrors(fieldErrors)
              .build();

      return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(com.zemenbank.atmMonitoring.exception.ValidationException.class)
  public ResponseEntity<ValidationErrorResponse> handleValidationException(
          com.zemenbank.atmMonitoring.exception.ValidationException ex, WebRequest request) {
      
      ValidationErrorResponse errorResponse = ValidationErrorResponse.builder()
              .timestamp(LocalDateTime.now())
              .status(HttpStatus.BAD_REQUEST.value())
              .error("Validation Error")
              .message(ex.getMessage())
              .build();

      return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
}

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Object> globalExceptionHandler(Exception ex, WebRequest request) {
    // Check if it's a validation-related exception that wasn't caught
    if (ex.getMessage() != null && ex.getMessage().contains("Validation failed")) {
        Map<String, String> fieldErrors = extractFieldErrorsFromMessage(ex.getMessage());
        
        String message;
        if (!fieldErrors.isEmpty()) {
            String fieldNames = String.join(", ", fieldErrors.keySet());
            message = "Please fix the following fields: " + fieldNames;
        } else {
            message = "Please check your input and fix the validation errors";
        }
        
        ValidationErrorResponse errorResponse = ValidationErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Error")
                .message(message)
                .validationErrors(fieldErrors.isEmpty() ? null : fieldErrors)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
         // For other server errors
    ErrorMessage message = new ErrorMessage(
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        new Date(),
         "An unexpected error occurred. Please try again later.",
        request.getDescription(false));
    
     return new ResponseEntity<Object>(message, HttpStatus.INTERNAL_SERVER_ERROR);
  }
 @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorMessage> handleDuplicateEntryException(DataIntegrityViolationException  ex,WebRequest request) {
        // Create a custom error response
        ErrorMessage message = new ErrorMessage(
        HttpStatus.CONFLICT.value(),
        new Date(),
        "Data already exists",
        request.getDescription(false));
        return new ResponseEntity<>(message, HttpStatus.CONFLICT);
    }

    /**
     * Extract field errors from Spring's validation error message
     */
    private Map<String, String> extractFieldErrorsFromMessage(String errorMessage) {
        Map<String, String> fieldErrors = new HashMap<>();
        
        try {
            // Parse the long validation error message to extract field-specific errors
            String[] fieldErrorSections = errorMessage.split("\\[Field error in object");
            
            for (String section : fieldErrorSections) {
                if (section.contains("on field")) {
                    // Extract field name
                    String fieldName = extractFieldName(section);
                    // Extract default message
                    String message = extractDefaultMessage(section);
                    
                    if (fieldName != null && message != null) {
                        fieldErrors.put(fieldName, message);
                    }
                }
            }
        } catch (Exception e) {
            // If parsing fails, return empty map
            System.err.println("Failed to parse validation error message: " + e.getMessage());
        }
        
        return fieldErrors;
    }
    
    private String extractFieldName(String section) {
        try {
            int startIndex = section.indexOf("on field '") + 10;
            int endIndex = section.indexOf("':", startIndex);
            if (startIndex > 9 && endIndex > startIndex) {
                return section.substring(startIndex, endIndex);
            }
        } catch (Exception e) {
            // Ignore parsing errors
        }
        return null;
    }
    
    private String extractDefaultMessage(String section) {
        try {
            int startIndex = section.lastIndexOf("default message [") + 17;
            int endIndex = section.lastIndexOf("]]");
            if (startIndex > 16 && endIndex > startIndex) {
                return section.substring(startIndex, endIndex);
            }
        } catch (Exception e) {
            // Ignore parsing errors
        }
        return null;
    }

}
