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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.skillstormproject1.batstats.dtos.InventoryItemDTO;
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
   
   // search items 
   @GetMapping("/search")
   public ResponseEntity<List<InventoryItem>> searchItems(@RequestParam String term) {
      return ResponseEntity.ok(inventoryItemService.searchItems(term));
   }

   /**
    * createInventoryItem PostMapping
    * updateInventoryItem PutMapping
    * deleteInvntoryItem DeleteMapping
    * 
    */

   @PostMapping
    public ResponseEntity<InventoryItem> createItem(@RequestBody InventoryItemDTO dto) {
         InventoryItem created = inventoryItemService.createInventoryItem(dto);
      return ResponseEntity.status(HttpStatus.CREATED).body(created);
   }
   
   @PutMapping("/{id}")
    public ResponseEntity<InventoryItem> updateItem(@PathVariable Integer id, @RequestBody InventoryItemDTO dto) {
      return ResponseEntity.ok(inventoryItemService.updateInventoryItem(id, dto));
   }

   @DeleteMapping("/{id}")
   public ResponseEntity<Void> deleteItem(@PathVariable Integer id) {
         inventoryItemService.deleteInventoryItem(id);
      return ResponseEntity.noContent().build();
   }

   @GetMapping("/multi-location")
    public ResponseEntity<List<InventoryItem>> getMultiLocationItems() {
      return ResponseEntity.ok(inventoryItemService.getItemsInMultipleWarehouses());
   }
}
