package com.skillstormproject1.batstats.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // find all inventory items of a certain product type
    List<InventoryItem> findByProductTypeId(Integer productTypeId);

    // search itesm by serial number or product name
    @Query("SELECT DISTINCT i FROM InventoryItem i " +
           "LEFT JOIN FETCH i.productType pt " +
           "WHERE LOWER(i.serialNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(pt.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<InventoryItem> searchItems(@Param("searchTerm") String searchTerm);

    // find items with multiple warehouse locations
    @Query("SELECT i FROM InventoryItem i " +
           "WHERE SIZE(i.warehouseLocations) > 1")
    List<InventoryItem> findItemsInMultipleWarehouses();

    // find items not in any warehouse 
    @Query("SELECT i FROM InventoryItem i " +
           "WHERE SIZE(i.warehouseLocations) = 0")
    List<InventoryItem> findItemsWithoutLocation();


}
