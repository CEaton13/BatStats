package com.skillstormproject1.batstats.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.skillstormproject1.batstats.models.InventoryItem;
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
            WarehouseRepository warehouseRepository, ProductTypeRepository productTypeRepository) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.warehouseRepository = warehouseRepository;
        this.productTypeRepository = productTypeRepository;
    }

    // finds all inventory items 
    public List<InventoryItem> getAllInventoryItems(){
        return inventoryItemRepository.findAll();
    }

    // get item by inventory id
    public InventoryItem getInventoryItemById(int id){
        return inventoryItemRepository.findById(id).orElseThrow();
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

}
