package com.skillstormproject1.batstats.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.skillstormproject1.batstats.models.InventoryItem;
import com.skillstormproject1.batstats.services.InventoryItemService;

@RestController
public class InventoryItemController {
    
   private final InventoryItemService inventoryItemService;

   public InventoryItemController(InventoryItemService inventoryItemService) {
      this.inventoryItemService = inventoryItemService;
   }

   // get a list of all inventory items
   @GetMapping
    public ResponseEntity<List<InventoryItem>> getAllInventoryItems() {
        return ResponseEntity.ok(inventoryItemService.getAllInventoryItems());
   }

   // get to find inventory by id
   @GetMapping("/{id}")
   public ResponseEntity<InventoryItem> getInventoryItemById(@PathVariable int id) {
         return ResponseEntity.ok(inventoryItemService.getInventoryItemById(id));
   }
   
   // get finds all items in a specific warehouse
   @GetMapping("/warehouse/{warehouseId}")
   public ResponseEntity<List<InventoryItem>> getItemsByWarehouse(@PathVariable int warehouseId) {
      return ResponseEntity.ok(inventoryItemService.getItemsByWarehouse(warehouseId));
   }

   // get will find items by product type
   @GetMapping("/product-type/{productTypeId}")
   public ResponseEntity<List<InventoryItem>> getItemsByProductType(@PathVariable int productTypeId) {
      return ResponseEntity.ok(inventoryItemService.getItemsByProductType(productTypeId));
   }

   /**
    * createInventoryItem PostMapping
    * updateInventoryItem PutMapping
    * deleteInvntoryItem DeleteMapping
    * 
    */
}
