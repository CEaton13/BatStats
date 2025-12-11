package com.skillstormproject1.batstats.controller;

import java.util.List;
import java.util.Map;

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
import org.springframework.web.bind.annotation.RestController;

import com.skillstormproject1.batstats.dtos.AddItemToWarehouseDTO;
import com.skillstormproject1.batstats.dtos.TransferRequestDTO;
import com.skillstormproject1.batstats.models.WarehouseInventory;
import com.skillstormproject1.batstats.services.WarehouseInventoryService;

@RestController
@RequestMapping("/api/warehouse-inventory")
@CrossOrigin(origins = "http://localhost:5500")
public class WarehouseInventoryController {

    private final WarehouseInventoryService warehouseInventoryService;

    public WarehouseInventoryController(WarehouseInventoryService warehouseInventoryService) {
        this.warehouseInventoryService = warehouseInventoryService;
    }

    // get all items in a warehouse
    @GetMapping("/warehouse/{id}")
    public ResponseEntity<List<WarehouseInventory>> getItemsInWarehouse(@PathVariable Integer id) {
        List<WarehouseInventory> items = warehouseInventoryService.getItemsInWarehouse(id);
        return ResponseEntity.ok(items);
    }

    // get all locations for an item
    @GetMapping("/item/{id}")
    public ResponseEntity<List<WarehouseInventory>> getLocationsForItem(@PathVariable Integer id) {
        List<WarehouseInventory> locations = warehouseInventoryService.getLocationsForItem(id);
        return ResponseEntity.ok(locations);
    }

    // get total quantity across all warehouses 
    @GetMapping("/item/{id}/total")
    public ResponseEntity<Map<String, Integer>> getTotalQuantity(@PathVariable Integer id) {
        Integer total = warehouseInventoryService.getTotalQuantityForItem(id);
        return ResponseEntity.ok(Map.of("totalQuantity", total));
    }

    // post add item to warehouse location
    @PostMapping
    public ResponseEntity<WarehouseInventory> addItemToWarehouse(@RequestBody AddItemToWarehouseDTO dto) {
        WarehouseInventory location = warehouseInventoryService.addItemToWarehouse(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(location);
    }

    // put to update quantity at location
    @PutMapping("/{id}")
    public ResponseEntity<WarehouseInventory> updateQuantity(@PathVariable Integer id,@RequestBody Map<String, Integer> request) {
        Integer newQuantity = request.get("quantity");
        WarehouseInventory updated = warehouseInventoryService.updateQuantityAtLocation(id, newQuantity);
        return ResponseEntity.ok(updated);
    }

    // delete item from warehouse 
    @DeleteMapping("/warehouse/{warehouseId}/item/{itemId}")
    public ResponseEntity<Void> removeItemFromWarehouse(@PathVariable Integer warehouseId, @PathVariable Integer itemId) {
        warehouseInventoryService.removeItemFromWarehouse(warehouseId, itemId);
        return ResponseEntity.noContent().build();
    }

    // post to transfer item between warehouses 
    @PostMapping("/transfer")
    public ResponseEntity<Void> transferBetweenWarehouses(@RequestBody TransferRequestDTO transferRequest) {
        warehouseInventoryService.transferBetweenWarehouses(
            transferRequest.getItemId(),
            transferRequest.getSourceWarehouseId(),
            transferRequest.getDestinationWarehouseId(),
            transferRequest.getQuantity()
        );
        return ResponseEntity.ok().build();
    }
    
}
