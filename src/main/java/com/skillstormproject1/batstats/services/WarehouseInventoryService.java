package com.skillstormproject1.batstats.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.skillstormproject1.batstats.dtos.AddItemToWarehouseDTO;
import com.skillstormproject1.batstats.exceptions.DuplicateSerialNumberException;
import com.skillstormproject1.batstats.exceptions.ResourceNotFoundException;
import com.skillstormproject1.batstats.exceptions.WarehouseCapacityExceededException;
import com.skillstormproject1.batstats.models.InventoryItem;
import com.skillstormproject1.batstats.models.Warehouse;
import com.skillstormproject1.batstats.models.WarehouseInventory;
import com.skillstormproject1.batstats.repositories.InventoryItemRepository;
import com.skillstormproject1.batstats.repositories.WarehouseInventoryRepository;
import com.skillstormproject1.batstats.repositories.WarehouseRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class WarehouseInventoryService {

    private final WarehouseInventoryRepository warehouseInventoryRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryItemRepository inventoryItemRepository;
    
    public WarehouseInventoryService(WarehouseInventoryRepository warehouseInventoryRepository,
            WarehouseRepository warehouseRepository, InventoryItemRepository inventoryItemRepository) {
        this.warehouseInventoryRepository = warehouseInventoryRepository;
        this.warehouseRepository = warehouseRepository;
        this.inventoryItemRepository = inventoryItemRepository;
    }

    // get all items in a warehouse
    public List<WarehouseInventory> getItemsInWarehouse(Integer warehouseId) {
        return warehouseInventoryRepository.findByWarehouseId(warehouseId);
    }

    // get all warehouse locations for an item
    public List<WarehouseInventory> getLocationsForItem(Integer itemId) {
        return warehouseInventoryRepository.findByInventoryItemId(itemId);
    }

    // get total quantity of an item across all warehouses
    public Integer getTotalQuantityForItem(Integer itemId) {
        Integer total = warehouseInventoryRepository.getTotalQuantityForItem(itemId);
        return total != null ? total : 0;
    }

    // check if specific location exists
    public boolean itemExistsInWarehouse(Integer warehouseId, Integer itemId) {
        return warehouseInventoryRepository
            .existsByWarehouseIdAndInventoryItemId(warehouseId, itemId);
    }


    /**
    * create - add items to warehouse
    * update - update quantity
    * delete - remove from warehouse 
    */

    // add an existing item to a warehouse location

    public WarehouseInventory addItemToWarehouse(AddItemToWarehouseDTO dto) {
        InventoryItem item = inventoryItemRepository.findById(dto.getInventoryItemId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Inventory item not found with id: " + dto.getInventoryItemId()));
        
        Warehouse warehouse = warehouseRepository.findById(dto.getWarehouseId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Warehouse not found with id: " + dto.getWarehouseId()));
        
        // Check if item already in this warehouse
        if (warehouseInventoryRepository.existsByWarehouseIdAndInventoryItemId(
                dto.getWarehouseId(), dto.getInventoryItemId())) {
            throw new DuplicateSerialNumberException(
                "Item " + item.getSerialNumber() + " already exists in warehouse " + warehouse.getName());
        }
        
        // Check warehouse capacity
        if (!warehouse.hasCapacity(dto.getQuantity())) {
            throw new WarehouseCapacityExceededException(
                String.format("Warehouse '%s' has insufficient capacity. Available: %d, Required: %d",
                    warehouse.getName(), warehouse.getAvailableCapacity(), dto.getQuantity()));
        }
        
        // Create junction table entry
        WarehouseInventory location = new WarehouseInventory(warehouse, item, dto.getQuantity());
        WarehouseInventory saved = warehouseInventoryRepository.save(location);
        
        // Database trigger automatically updates warehouse capacity
        
        return saved;
    }


    // update quantity of an item at a warehouse location
    public WarehouseInventory updateQuantityAtLocation(Integer locationId, Integer newQuantity) {
        WarehouseInventory location = warehouseInventoryRepository.findById(locationId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Warehouse location not found with id: " + locationId));
        
        int quantityDifference = newQuantity - location.getQuantity();
        
        // If increasing quantity, check capacity
        if (quantityDifference > 0) {
            Warehouse warehouse = location.getWarehouse();
            if (!warehouse.hasCapacity(quantityDifference)) {
                throw new WarehouseCapacityExceededException(
                    String.format("Warehouse '%s' has insufficient capacity for additional %d units",
                        warehouse.getName(), quantityDifference));
            }
        }
        
        location.setQuantity(newQuantity);
        return warehouseInventoryRepository.save(location);
        
        // Database trigger automatically updates warehouse capacity
    }


    // remove item from a warehouse location entirely 
    public void removeItemFromWarehouse(Integer warehouseId, Integer itemId) {
        WarehouseInventory location = warehouseInventoryRepository
            .findByWarehouseIdAndInventoryItemId(warehouseId, itemId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Item not found in specified warehouse"));
        
        warehouseInventoryRepository.delete(location);
        
        // Database trigger automatically updates warehouse capacity
    }
    
    // transfer quantity from one warehouse to another
    public void transferBetweenWarehouses(Integer itemId, Integer sourceWarehouseId, 
                                          Integer destinationWarehouseId, Integer quantity) {
        // Validate source location
        WarehouseInventory sourceLocation = warehouseInventoryRepository
            .findByWarehouseIdAndInventoryItemId(sourceWarehouseId, itemId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Item not found in source warehouse"));
        
        if (sourceLocation.getQuantity() < quantity) {
            throw new IllegalArgumentException(
                "Insufficient quantity at source. Available: " + sourceLocation.getQuantity());
        }
        
        // Get destination warehouse
        Warehouse destinationWarehouse = warehouseRepository.findById(destinationWarehouseId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Destination warehouse not found"));
        
        // Check destination capacity
        if (!destinationWarehouse.hasCapacity(quantity)) {
            throw new WarehouseCapacityExceededException(
                "Destination warehouse has insufficient capacity");
        }
        
        // Update source: reduce quantity or remove
        if (sourceLocation.getQuantity().equals(quantity)) {
            // Transferring all - remove from source
            warehouseInventoryRepository.delete(sourceLocation);
        } else {
            // Transferring partial - reduce quantity
            sourceLocation.setQuantity(sourceLocation.getQuantity() - quantity);
            warehouseInventoryRepository.save(sourceLocation);
        }
        
        // Update destination: increase quantity or create new location
        warehouseInventoryRepository
            .findByWarehouseIdAndInventoryItemId(destinationWarehouseId, itemId)
            .ifPresentOrElse(
                // Item already exists at destination - increase quantity
                destLocation -> {
                    destLocation.setQuantity(destLocation.getQuantity() + quantity);
                    warehouseInventoryRepository.save(destLocation);
                },
                // Item doesn't exist at destination - create new location
                () -> {
                    InventoryItem item = sourceLocation.getInventoryItem();
                    WarehouseInventory newLocation = new WarehouseInventory(
                        destinationWarehouse, item, quantity);
                    warehouseInventoryRepository.save(newLocation);
                }
            );
        
        // Database triggers automatically update warehouse capacities
    }

    
}
