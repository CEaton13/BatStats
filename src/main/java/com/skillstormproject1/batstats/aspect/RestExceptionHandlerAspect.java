package com.skillstormproject1.batstats.aspect;


import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.skillstormproject1.batstats.exceptions.DuplicateSerialNumberException;
import com.skillstormproject1.batstats.exceptions.ResourceNotFoundException;
import com.skillstormproject1.batstats.exceptions.WarehouseCapacityExceededException;

@Aspect
@Component
@Order(1)
public class RestExceptionHandlerAspect {

    private static final Logger logger = LoggerFactory.getLogger(RestExceptionHandlerAspect.class);

    @Around("execution(* com.batstats.controller..*(..))")
    public Object handleControllerExceptions(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (ResourceNotFoundException ex) {
            logger.error("Resource not found: {}", ex.getMessage());
            return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
        } catch (WarehouseCapacityExceededException ex) {
            logger.error("Warehouse capacity exceeded: {}", ex.getMessage());
            return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (DuplicateSerialNumberException ex) {
            logger.error("Duplicate serial number: {}", ex.getMessage());
            return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT);
        } catch (IllegalArgumentException ex) {
            logger.error("Invalid argument: {}", ex.getMessage());
            return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            logger.error("Unexpected error in controller", ex);
            return buildErrorResponse("An unexpected error occurred: " + ex.getMessage(), 
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(String message, HttpStatus status) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", message);
        return ResponseEntity.status(status).body(errorResponse);
    }
}
