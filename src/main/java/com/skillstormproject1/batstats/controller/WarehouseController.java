package com.skillstormproject1.batstats.controller;

import org.springframework.web.bind.annotation.RestController;

import com.skillstormproject1.batstats.dtos.WarehouseDTO;
import com.skillstormproject1.batstats.models.Warehouse;
import com.skillstormproject1.batstats.services.WarehouseService;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;


@RestController
@RequestMapping("/api/warehouses")
@CrossOrigin(origins = "http://127.0.0.1:5500")
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

    @PostMapping
    public ResponseEntity<Warehouse> createWarehouse(@RequestBody WarehouseDTO warehouseDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(warehouseService.createWarehouse(warehouseDTO));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Warehouse> updateWarehouse(@PathVariable int id, @RequestBody WarehouseDTO warehouseDTO) {
        return ResponseEntity.ok(warehouseService.updateWarehouse(id, warehouseDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWarehouse(@PathVariable int id) {
        warehouseService.deleteWarehouse(id);
        return ResponseEntity.noContent().build();
    }
}
