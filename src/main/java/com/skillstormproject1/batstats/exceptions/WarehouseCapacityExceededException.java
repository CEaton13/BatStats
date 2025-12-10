package com.skillstormproject1.batstats.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class WarehouseCapacityExceededException extends RuntimeException {
    public WarehouseCapacityExceededException(String message) {
        super(message);
    }
}
