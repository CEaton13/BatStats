package com.skillstormproject1.batstats.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.skillstormproject1.batstats.models.InventoryItem;
import com.skillstormproject1.batstats.repositories.InventoryItemRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class InventoryItemService {

    private final InventoryItemRepository inventoryItemRepository;

    public InventoryItemService(InventoryItemRepository inventoryItemRepository) {
        this.inventoryItemRepository = inventoryItemRepository;
    }


    // finds all inventory items 
    public List<InventoryItem> getAllInventoryItems(){
        return inventoryItemRepository.findAll();
    }

    // get item by inventory id
    public InventoryItem getInventoryItemById(int id){
        return inventoryItemRepository.findById(id).orElseThrow();
    }

    
    /**
     * createInventoryItem - set all params for item like product type then put it into a warehouse save 
     * updateInventoryItem - update quantity which will affect warehouse capacity
     * deleteInvntoryItem - this will also affect warehouse capacity
     */

}
