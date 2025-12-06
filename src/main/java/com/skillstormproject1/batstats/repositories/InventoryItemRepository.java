package com.skillstormproject1.batstats.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.skillstormproject1.batstats.models.InventoryItem;

@Repository     // Repository Interface for InventoryItems
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Integer> {

    /**
     *  Services
     *      findBySerialNum
     *      findByWarehouse
     *      findByProductType
     *      existsSerialNum
     *      search
     */

}
