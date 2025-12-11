package com.skillstormproject1.batstats.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.skillstormproject1.batstats.dtos.InventoryItemDTO;
import com.skillstormproject1.batstats.exceptions.DuplicateSerialNumberException;
import com.skillstormproject1.batstats.exceptions.ResourceNotFoundException;
import com.skillstormproject1.batstats.models.InventoryItem;
import com.skillstormproject1.batstats.models.ProductType;
import com.skillstormproject1.batstats.repositories.InventoryItemRepository;
import com.skillstormproject1.batstats.repositories.ProductTypeRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class InventoryItemService {

    private final InventoryItemRepository inventoryItemRepository;
    private final ProductTypeRepository productTypeRepository;
    
    public InventoryItemService(InventoryItemRepository inventoryItemRepository,
                               ProductTypeRepository productTypeRepository) {
        this.inventoryItemRepository = inventoryItemRepository;
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
    /**
     * createInventoryItem - set all params for item like product type then put it into a warehouse save 
     * updateInventoryItem - update quantity which will affect warehouse capacity
     * deleteInvntoryItem - this will also affect warehouse capacity
     */
    
    public InventoryItem createInventoryItem(InventoryItemDTO itemDTO) {
        // Check for duplicate serial number
        if (inventoryItemRepository.existsBySerialNumber(itemDTO.getSerialNumber())) {
            throw new DuplicateSerialNumberException(
                "Item with serial number " + itemDTO.getSerialNumber() + " already exists");
        }
                // Get product type
        ProductType productType = productTypeRepository.findById(itemDTO.getProductTypeId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Product type not found with id: " + itemDTO.getProductTypeId()));
        
        // Create item (much simpler now!)
        InventoryItem item = new InventoryItem();
        item.setSerialNumber(itemDTO.getSerialNumber());
        item.setProductType(productType);
        
        return inventoryItemRepository.save(item);
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
        
        inventoryItemRepository.deleteById(id);
    }
}
