package com.skillstormproject1.batstats.repositories;

import java.util.List;
import java.util.Optional;

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

    // find a item by the serial num
    Optional<InventoryItem> findBySerialNumber(String serialNumber);

    // check if a serial num exists
    boolean existsBySerialNumber(String serialNumber);

    // find all inventory items in a specific warehouse
    List<InventoryItem> findByWarehouseId(Integer warehouseId);

    // find all inventory items of a certain product type
    List<InventoryItem> findByProductTypeId(Integer productTypeId);


}
