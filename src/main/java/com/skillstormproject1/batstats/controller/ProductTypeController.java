package com.skillstormproject1.batstats.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.skillstormproject1.batstats.models.InventoryItem;
import com.skillstormproject1.batstats.services.InventoryItemService;
import com.skillstormproject1.batstats.services.ProductTypeService;

@RestController
public class ProductTypeController {

    private final InventoryItemService inventoryItemService;

    private final ProductTypeService productTypeService;

    public ProductTypeController(ProductTypeService productTypeService, InventoryItemService inventoryItemService) {
        this.productTypeService = productTypeService;
        this.inventoryItemService = inventoryItemService;
    }

    // get to find inventory by id
    @GetMapping("/{id}")
    public ResponseEntity<InventoryItem> getInventoryItemById(@PathVariable int id) {
        return ResponseEntity.ok(inventoryItemService.getInventoryItemById(id));
    }
    
    
}
