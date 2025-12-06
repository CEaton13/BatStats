package com.skillstormproject1.batstats.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.skillstormproject1.batstats.models.Warehouse;

@Repository         // Repository Interface for Warehouse
public interface WarehouseRepository extends JpaRepository<Warehouse, Integer> {

    /**
     *  Services
     *      Find out the capacity of a warehouse
     * 
    */

    // Find all the Warehouses that are active
    List<Warehouse> findByStatus(String status);

    // 


}
