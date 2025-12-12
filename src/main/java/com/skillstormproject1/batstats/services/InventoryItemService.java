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
import com.skillstormproject1.batstats.models.WarehouseInventory;
import com.skillstormproject1.batstats.repositories.InventoryItemRepository;
import com.skillstormproject1.batstats.repositories.ProductTypeRepository;
import com.skillstormproject1.batstats.repositories.WarehouseInventoryRepository;
import com.skillstormproject1.batstats.repositories.WarehouseRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class InventoryItemService {

    private final InventoryItemRepository inventoryItemRepository;
    private final ProductTypeRepository productTypeRepository;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseInventoryRepository warehouseInventoryRepository;
    
    public InventoryItemService(InventoryItemRepository inventoryItemRepository,
                               ProductTypeRepository productTypeRepository,
                               WarehouseRepository warehouseRepository,
                               WarehouseInventoryRepository warehouseInventoryRepository) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.productTypeRepository = productTypeRepository;
        this.warehouseRepository = warehouseRepository;
        this.warehouseInventoryRepository = warehouseInventoryRepository;
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

    // get by serial number
    public InventoryItem getInventoryItemBySerialNumber(String serialNumber) {
        return inventoryItemRepository.findBySerialNumber(serialNumber)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Inventory item not found with serial number: " + serialNumber));
    }

    // find and list all items of a product type
    public List<InventoryItem> getItemsByProductType(Integer productTypeId) {
        return inventoryItemRepository.findByProductTypeId(productTypeId);
    }
    
    // search through the items
    public List<InventoryItem> searchItems(String searchTerm) {
        return inventoryItemRepository.searchItems(searchTerm);
    }

    // get items from multiple warehouses
    public List<InventoryItem> getItemsInMultipleWarehouses() {
        return inventoryItemRepository.findItemsInMultipleWarehouses();
    }

    // get the items without using the location 
    public List<InventoryItem> getItemsWithoutLocation() {
        return inventoryItemRepository.findItemsWithoutLocation();
    }

    private String generateSerialNumber(ProductType productType) {
        // Get product category prefix (first 3 letters uppercase)
        String categoryPrefix = productType.getCategory().length() >= 3 
            ? productType.getCategory().substring(0, 3).toUpperCase()
            : productType.getCategory().toUpperCase();
        
        // Count existing items of this product type to get next number
        int existingCount = inventoryItemRepository.findByProductTypeId(productType.getId()).size();
        int nextNumber = existingCount + 1;
        
        String serialNumber;
        int attempts = 0;
        
        // Keep trying until we find a unique serial number
        do {
            serialNumber = String.format("%s-%03d", categoryPrefix, nextNumber + attempts);
            attempts++;
        } while (inventoryItemRepository.existsBySerialNumber(serialNumber) && attempts < 1000);
        
        if (attempts >= 1000) {
            throw new IllegalStateException("Unable to generate unique serial number after 1000 attempts");
        }
        
        return serialNumber;
    }

    /**
     * createInventoryItem - set all params for item like product type then put it into a warehouse save 
     * updateInventoryItem - update quantity which will affect warehouse capacity
     * deleteInvntoryItem - this will also affect warehouse capacity
     */
    
    public InventoryItem createInventoryItem(InventoryItemDTO itemDTO) {
        // Get product type
        ProductType productType = productTypeRepository.findById(itemDTO.getProductTypeId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Product type not found with id: " + itemDTO.getProductTypeId()));
        
        // Generate serial number if not provided
        String serialNumber = itemDTO.getSerialNumber();
        if (serialNumber == null || serialNumber.trim().isEmpty()) {
            serialNumber = generateSerialNumber(productType);
        } else {
            // Check for duplicate if provided
            if (inventoryItemRepository.existsBySerialNumber(serialNumber)) {
                throw new DuplicateSerialNumberException(
                    "Item with serial number " + serialNumber + " already exists");
            }
        }
        
        // Create the inventory item
        InventoryItem item = new InventoryItem();
        item.setSerialNumber(serialNumber);
        item.setProductType(productType);
        
        // Save the item first
        item = inventoryItemRepository.save(item);
        
        // If warehouse assignment provided, add to warehouse
        if (itemDTO.getInitialWarehouseId() != null && itemDTO.getInitialQuantity() != null) {
            Warehouse warehouse = warehouseRepository.findById(itemDTO.getInitialWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Warehouse not found with id: " + itemDTO.getInitialWarehouseId()));
            
            // Check warehouse capacity
            if (!warehouse.hasCapacity(itemDTO.getInitialQuantity())) {
                throw new WarehouseCapacityExceededException(
                    String.format("Warehouse '%s' has insufficient capacity. Available: %d, Required: %d",
                        warehouse.getName(), warehouse.getAvailableCapacity(), itemDTO.getInitialQuantity()));
            }
            
            // Create warehouse location entry
            WarehouseInventory location = new WarehouseInventory(warehouse, item, itemDTO.getInitialQuantity());
            warehouseInventoryRepository.save(location);
            
            // Database trigger will automatically update warehouse capacity
        }
        
        return item;
    }
    
    public InventoryItem updateInventoryItem(Integer id, InventoryItemDTO itemDTO) {
        InventoryItem existing = getInventoryItemById(id);
        
        // Only product type can be updated
        if (itemDTO.getProductTypeId() != null) {
            ProductType productType = productTypeRepository.findById(itemDTO.getProductTypeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Product type not found with id: " + itemDTO.getProductTypeId()));
            existing.setProductType(productType);
        }
        
        return inventoryItemRepository.save(existing);
    }
    
    
    public void deleteInventoryItem(Integer id) {
        if (!inventoryItemRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                "Inventory item not found with id: " + id);
        }
        // cascade delete to handle the warehouse inventory items
        inventoryItemRepository.deleteById(id);
    }
}
