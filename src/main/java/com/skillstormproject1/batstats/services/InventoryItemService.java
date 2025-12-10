package com.skillstormproject1.batstats.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.skillstormproject1.batstats.dtos.InventoryItemDTO;
import com.skillstormproject1.batstats.models.InventoryItem;
import com.skillstormproject1.batstats.models.ProductType;
import com.skillstormproject1.batstats.models.Warehouse;
import com.skillstormproject1.batstats.repositories.InventoryItemRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class InventoryItemService {

    private final InventoryItemRepository inventoryItemRepository;
    private final WarehouseService warehouseService;
    private final ProductTypeService productTypeService;

    public InventoryItemService(InventoryItemRepository inventoryItemRepository, WarehouseService warehouseService,
            ProductTypeService productTypeService) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.warehouseService = warehouseService;
        this.productTypeService = productTypeService;
    }

    // finds all inventory items 
    public List<InventoryItem> getAllInventoryItems(){
        return inventoryItemRepository.findAll();
    }

    // get item by inventory id
    public InventoryItem getInventoryItemById(int id){
        return inventoryItemRepository.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Iventory item not found with id: " + id));
    }

    // get all items in a specific warehouse
    public List<InventoryItem> getItemsByWarehouse(int warehouseId){
        return inventoryItemRepository.findByWarehouseId(warehouseId);
    }

    // find and list all items of a product type
    public List<InventoryItem> getItemsByProductType(int productTypeId) {
        return inventoryItemRepository.findByProductType(productTypeId);
    }
    
    /**
     * createInventoryItem - set all params for item like product type then put it into a warehouse save 
     * updateInventoryItem - update quantity which will affect warehouse capacity
     * deleteInvntoryItem - this will also affect warehouse capacity
     */

    public InventoryItem createInventoryItem(InventoryItem item) {
        
        // Check for duplicate serial number
        if (inventoryItemRepository.existsBySerialNumber(item.getSerialNumber())) {
            throw new RuntimeException("Item with serial number '" + item.getSerialNumber() + "' already exists");
        }
        
        // Validate product type exists
        ProductType productType = productTypeService.getProductTypeById(item.getProductType().getId());
        item.setProductType(productType);
        
        // Validate warehouse exists and has capacity
        Warehouse warehouse = warehouseService.getWarehouseById(item.getWarehouse().getId());
        if (!warehouse.hasCapacityFor(item.getQuantity())) {
            throw new RuntimeException("Warehouse '" + warehouse.getName() + "' does not have sufficient capacity. " +
                                     "Available: " + warehouse.getAvailableCapacity() + 
                                     ", Required: " + item.getQuantity());
        }
        
        item.setWarehouse(warehouse);
        
        // Save the item
        InventoryItem savedItem = inventoryItemRepository.save(item);
        
        // Update warehouse capacity
        warehouseService.updateWarehouseCapacity(warehouse.getId(), item.getQuantity());

        return savedItem;
    }

    
    public InventoryItem updateInventoryItem(int id, InventoryItem itemDetails) {
    
        InventoryItem item = getInventoryItemById(id);
        Integer oldQuantity = item.getQuantity();
        int oldWarehouseId = item.getWarehouse().getId();
        
        // Check for serial number conflicts (excluding current item)
        if (!item.getSerialNumber().equals(itemDetails.getSerialNumber()) && 
            inventoryItemRepository.existsBySerialNumber(itemDetails.getSerialNumber())) {
            throw new RuntimeException("Item with serial number '" + itemDetails.getSerialNumber() + "' already exists");
        }
        
        // Update basic fields
        item.setSerialNumber(itemDetails.getSerialNumber());
        
        // Handle quantity change
        if (!oldQuantity.equals(itemDetails.getQuantity())) {
            Integer quantityDelta = itemDetails.getQuantity() - oldQuantity;
            
            Warehouse warehouse = item.getWarehouse();
            if (quantityDelta > 0 && !warehouse.hasCapacityFor(quantityDelta)) {
                throw new RuntimeException("Warehouse does not have sufficient capacity for quantity increase. " +
                                         "Available: " + warehouse.getAvailableCapacity() + 
                                         ", Required: " + quantityDelta);
            }
            
            item.setQuantity(itemDetails.getQuantity());
            warehouseService.updateWarehouseCapacity(oldWarehouseId, quantityDelta);
        }
        
        InventoryItem updatedItem = inventoryItemRepository.save(item);
        return updatedItem;
    }

    
    public void deleteInventoryItem(int id) {
        
        InventoryItem item = getInventoryItemById(id);
        
        // Update warehouse capacity
        warehouseService.updateWarehouseCapacity(item.getWarehouse().getId(), -item.getQuantity());
        
        inventoryItemRepository.deleteById(id);
    }

    

}
