package com.skillstormproject1.batstats.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.skillstormproject1.batstats.models.Warehouse;

@Repository         // Repository Interface for Warehouse
public interface WarehouseRepository extends JpaRepository<Warehouse, Integer> {

    /**
     *  Services
     *      Find out the capacity of a warehouse
     * 
    */

    // find all the Warehouses that are active
    List<Warehouse> findByStatus(String status);

    // find warehouses near a capacity based on the threshold variable passed
    @Query("SELECT w FROM Warehouse w WHERE (w.currentCapacity * 100.0 / w.maxCapacity) >= :threshold")
    List<Warehouse> findWarehousesNearCapacity(Double threshold);

    // find warehouse with available capacity greater than the amount passed 
    @Query("SELECT w FROM Warehouse w WHERE (w.maxCapacity - w.currentCapacity) >= :minCapacity")
    List<Warehouse> findWarehousesWithAvailableCapacity(Integer minCapacity);

}
