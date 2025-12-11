package com.skillstormproject1.batstats.controller;

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
import org.springframework.web.bind.annotation.RestController;

import com.skillstormproject1.batstats.dtos.InventoryItemDTO;
import com.skillstormproject1.batstats.dtos.TransferRequestDTO;
import com.skillstormproject1.batstats.models.InventoryItem;
import com.skillstormproject1.batstats.services.InventoryItemService;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "http://127.0.0.1:5500")
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
   public ResponseEntity<InventoryItem> getInventoryItemById(@PathVariable Integer id) {
         return ResponseEntity.ok(inventoryItemService.getInventoryItemById(id));
   }
   
   // get finds all items in a specific warehouse
   @GetMapping("/warehouse/{warehouseId}")
   public ResponseEntity<List<InventoryItem>> getItemsByWarehouse(@PathVariable Integer warehouseId) {
      return ResponseEntity.ok(inventoryItemService.getItemsByWarehouse(warehouseId));
   }

   // get will find items by product type
   @GetMapping("/product-type/{productTypeId}")
   public ResponseEntity<List<InventoryItem>> getItemsByProductType(@PathVariable Integer productTypeId) {
      return ResponseEntity.ok(inventoryItemService.getItemsByProductType(productTypeId));
   }

   /**
    * createInventoryItem PostMapping
    * updateInventoryItem PutMapping
    * deleteInvntoryItem DeleteMapping
    * 
    */

   @PostMapping
    public ResponseEntity<InventoryItem> createInventoryItem(@RequestBody InventoryItemDTO itemDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(inventoryItemService.createInventoryItem(itemDTO));
   }

   @PutMapping("/{id}")
    public ResponseEntity<InventoryItem> updateInventoryItem(@PathVariable Integer id, @RequestBody InventoryItemDTO itemDTO) {
        return ResponseEntity.ok(inventoryItemService.updateInventoryItem(id, itemDTO));
   }

   @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventoryItem(@PathVariable Integer id) {
        inventoryItemService.deleteInventoryItem(id);
        return ResponseEntity.noContent().build();
   }

   @PostMapping("/transfer")
    public ResponseEntity<InventoryItem> transferItem(@RequestBody TransferRequestDTO transferDTO) {
        return ResponseEntity.ok(inventoryItemService.transferItem(transferDTO));
   }

}
