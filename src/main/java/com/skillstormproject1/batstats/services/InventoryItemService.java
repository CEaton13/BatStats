package com.skillstormproject1.batstats.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.skillstormproject1.batstats.dtos.InventoryItemDTO;
import com.skillstormproject1.batstats.exceptions.DuplicateSerialNumberException;
import com.skillstormproject1.batstats.exceptions.ResourceNotFoundException;
import com.skillstormproject1.batstats.exceptions.WarehouseCapacityExceededException;
import com.skillstormproject1.batstats.models.InventoryItem;
import com.skillstormproject1.batstats.models.ProductType;
import com.skillstormproject1.batstats.models.Warehouse;
import com.skillstormproject1.batstats.repositories.InventoryItemRepository;
import com.skillstormproject1.batstats.repositories.ProductTypeRepository;
import com.skillstormproject1.batstats.repositories.WarehouseRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class InventoryItemService {

    private final InventoryItemRepository inventoryItemRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductTypeRepository productTypeRepository;
    
    public InventoryItemService(InventoryItemRepository inventoryItemRepository,
                               WarehouseRepository warehouseRepository,
                               ProductTypeRepository productTypeRepository) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.warehouseRepository = warehouseRepository;
        this.productTypeRepository = productTypeRepository;
    }

    // finds all inventory items 
    public List<InventoryItem> getAllInventoryItems(){
        return inventoryItemRepository.findAll();
    }

    // get item by inventory id
    public InventoryItem getInventoryItemById(Integer id){
        return inventoryItemRepository.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Iventory item not found with id: " + id));
    }

    // get all items in a specific warehouse
    public List<InventoryItem> getItemsByWarehouse(Integer warehouseId){
        return inventoryItemRepository.findByWarehouseId(warehouseId);
    }

    // find and list all items of a product type
    public List<InventoryItem> getItemsByProductType(Integer productTypeId) {
        return inventoryItemRepository.findByProductType(productTypeId);
    }
    
    /**
     * createInventoryItem - set all params for item like product type then put it into a warehouse save 
     * updateInventoryItem - update quantity which will affect warehouse capacity
     * deleteInvntoryItem - this will also affect warehouse capacity
     */
    
    public InventoryItem createInventoryItem(InventoryItemDTO itemDTO) {
        if (inventoryItemRepository.existsBySerialNumber(itemDTO.getSerialNumber())) {
            throw new DuplicateSerialNumberException(
                "Item with serial number " + itemDTO.getSerialNumber() + " already exists");
        }
        
        Warehouse warehouse = warehouseRepository.findById(itemDTO.getWarehouseId())
            .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + itemDTO.getWarehouseId()));
        
        ProductType productType = productTypeRepository.findById(itemDTO.getProductTypeId())
            .orElseThrow(() -> new ResourceNotFoundException("Product type not found with id: " + itemDTO.getProductTypeId()));
        
        validateWarehouseCapacity(warehouse, itemDTO.getQuantity());
        
        InventoryItem item = new InventoryItem();
        item.setSerialNumber(itemDTO.getSerialNumber());
        item.setProductType(productType);
        item.setWarehouse(warehouse);
        item.setQuantity(itemDTO.getQuantity());

        
        InventoryItem savedItem = inventoryItemRepository.save(item);
        
        warehouse.setCurrentCapacity(warehouse.getCurrentCapacity() + itemDTO.getQuantity());
        warehouseRepository.save(warehouse);
        
        return savedItem;
    }
    
    public InventoryItem updateInventoryItem(Integer id, InventoryItemDTO itemDTO) {
        InventoryItem existing = getInventoryItemById(id);
        Warehouse currentWarehouse = existing.getWarehouse();
        
        int quantityDifference = itemDTO.getQuantity() - existing.getQuantity();
        
        if (quantityDifference > 0) {
            validateWarehouseCapacity(currentWarehouse, quantityDifference);
        }
        
        existing.setQuantity(itemDTO.getQuantity());

        InventoryItem updatedItem = inventoryItemRepository.save(existing);
        
        currentWarehouse.setCurrentCapacity(currentWarehouse.getCurrentCapacity() + quantityDifference);
        warehouseRepository.save(currentWarehouse);
        
        return updatedItem;
    }
    
    
    public void deleteInventoryItem(Integer id) {
        InventoryItem item = getInventoryItemById(id);
        Warehouse warehouse = item.getWarehouse();
        
        warehouse.setCurrentCapacity(warehouse.getCurrentCapacity() - item.getQuantity());
        warehouseRepository.save(warehouse);
        
        inventoryItemRepository.deleteById(id);
    }

    
    // helper method to make sure that the warehouse has the avaliable capacity or throw error in createInventoryItem
    private void validateWarehouseCapacity(Warehouse warehouse, Integer quantity) {
        int availableCapacity = warehouse.getAvailableCapacity();
        if (quantity > availableCapacity) {
            throw new WarehouseCapacityExceededException(
                String.format("Warehouse '%s' has insufficient capacity. Available: %d, Required: %d",
                    warehouse.getName(), availableCapacity, quantity));
        }
    }
}
