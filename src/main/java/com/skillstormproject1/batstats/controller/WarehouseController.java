package com.skillstormproject1.batstats.controller;

import org.springframework.web.bind.annotation.RestController;

import com.skillstormproject1.batstats.models.Warehouse;
import com.skillstormproject1.batstats.services.WarehouseService;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
public class WarehouseController {

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    // get a list of all warehouses
    @GetMapping
    public ResponseEntity<List<Warehouse>> getAllWarehouses() {
        return ResponseEntity.ok(warehouseService.getAllWarehouses());
    }

    // get a warehouse by its id
    @GetMapping("/{id}")
    public ResponseEntity<Warehouse> getWarehouseById(@PathVariable int id) {
        return ResponseEntity.ok(warehouseService.getWarehouseById(id));
    }

    // get all warehouses by a specific status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Warehouse>> getWarehousesByStatus(@PathVariable String status) {
        return ResponseEntity.ok(warehouseService.getWarehouseByStatus(status));
    }


    /**
     * createWarehouse PostMapping
     * updateWarehouse PutMapping
     * deleteWarehouse DeleteMapping
     */

    
}
