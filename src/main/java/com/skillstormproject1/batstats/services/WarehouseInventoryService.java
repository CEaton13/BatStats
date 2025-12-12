package com.skillstormproject1.batstats.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(WarehouseInventoryService.class);

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
        
        logger.info("Added item {} to warehouse {} with quantity {}", 
            item.getSerialNumber(), warehouse.getName(), dto.getQuantity());
        
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
        
        Integer oldQuantity = location.getQuantity();
        location.setQuantity(newQuantity);
        WarehouseInventory updated = warehouseInventoryRepository.save(location);
        
        logger.info("Updated location {} quantity from {} to {}", 
            locationId, oldQuantity, newQuantity);
        
        // Database trigger automatically updates warehouse capacity
        
        return updated;
    }


    // remove item from a warehouse location entirely 
    public void removeItemFromWarehouse(Integer warehouseId, Integer itemId) {
        logger.info("Attempting to remove item {} from warehouse {}", itemId, warehouseId);
        
        WarehouseInventory location = warehouseInventoryRepository
            .findByWarehouseIdAndInventoryItemId(warehouseId, itemId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Item not found in specified warehouse"));
        
        logger.info("Found location with id {} to delete", location.getId());
        
        // Delete the location
        warehouseInventoryRepository.delete(location);
        
        // Flush to ensure immediate database update
        warehouseInventoryRepository.flush();
        
        logger.info("Successfully deleted location {} (item {} from warehouse {})", 
            location.getId(), itemId, warehouseId);
        
        // Verify deletion
        boolean stillExists = warehouseInventoryRepository.existsById(location.getId());
        if (stillExists) {
            logger.error("DELETION FAILED - Location {} still exists after delete!", location.getId());
        } else {
            logger.info("Deletion verified - Location {} no longer exists", location.getId());
        }
        
        // Database trigger automatically updates warehouse capacity
    }
    
    // transfer quantity from one warehouse to another
    public void transferBetweenWarehouses(Integer itemId, Integer sourceWarehouseId, 
                                          Integer destinationWarehouseId, Integer quantity) {
        logger.info("Transferring {} units of item {} from warehouse {} to warehouse {}", 
            quantity, itemId, sourceWarehouseId, destinationWarehouseId);
        
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
            logger.info("Transferring all {} units - removing from source", quantity);
            warehouseInventoryRepository.delete(sourceLocation);
        } else {
            // Transferring partial - reduce quantity
            Integer newSourceQty = sourceLocation.getQuantity() - quantity;
            logger.info("Transferring partial - reducing source from {} to {}", 
                sourceLocation.getQuantity(), newSourceQty);
            sourceLocation.setQuantity(newSourceQty);
            warehouseInventoryRepository.save(sourceLocation);
        }
        
        // Update destination: increase quantity or create new location
        warehouseInventoryRepository
            .findByWarehouseIdAndInventoryItemId(destinationWarehouseId, itemId)
            .ifPresentOrElse(
                // Item already exists at destination - increase quantity
                destLocation -> {
                    Integer newDestQty = destLocation.getQuantity() + quantity;
                    logger.info("Item exists at destination - increasing from {} to {}", 
                        destLocation.getQuantity(), newDestQty);
                    destLocation.setQuantity(newDestQty);
                    warehouseInventoryRepository.save(destLocation);
                },
                // Item doesn't exist at destination - create new location
                () -> {
                    logger.info("Creating new location at destination with quantity {}", quantity);
                    InventoryItem item = sourceLocation.getInventoryItem();
                    WarehouseInventory newLocation = new WarehouseInventory(
                        destinationWarehouse, item, quantity);
                    warehouseInventoryRepository.save(newLocation);
                }
            );
        
        logger.info("Transfer completed successfully");
        
        // Database triggers automatically update warehouse capacities
    }

    
}
