package com.skillstormproject1.batstats.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.skillstormproject1.batstats.models.WarehouseInventory;

@Repository
public interface WarehouseInventoryRepository extends JpaRepository<WarehouseInventory, Integer> {
    
    // find all items in a specific warehouse 
    List<WarehouseInventory> findByWarehouseId(Integer warehouseId);

    // find all warehouse locations for a specific item
    List<WarehouseInventory> findByInventoryItemId(Integer itemId);

    // find specific warehouse item combination
    Optional<WarehouseInventory> findByWarehouseIdAndInventoryItemId(Integer warehouseId, Integer itemId);

    // check if an item exists in a warehouse
    boolean existsByWarehouseIdAndInventoryItemId(Integer warehouseId, Integer itemId);
    
    // get total quantity of a specific item across all warehouses
    @Query("SELECT SUM(wi.quantity) FROM WarehouseInventory wi " +
           "WHERE wi.inventoryItem.id = :itemId")
    Integer getTotalQuantityForItem(@Param("itemId") Integer itemId);

    // get items low on stock in specific warehouse
    @Query("SELECT wi FROM WarehouseInventory wi " +
           "WHERE wi.warehouse.id = :warehouseId AND wi.quantity < :threshold")
    List<WarehouseInventory> findLowStockItems(@Param("warehouseId") Integer warehouseId, @Param("threshold") Integer threshold);
    
    // get warehouses containing a specific product type
    @Query("SELECT wi FROM WarehouseInventory wi " +
           "WHERE wi.inventoryItem.productType.id = :productTypeId")
    List<WarehouseInventory> findByProductType(@Param("productTypeId") Integer productTypeId);
}
