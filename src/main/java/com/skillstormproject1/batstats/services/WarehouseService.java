package com.skillstormproject1.batstats.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.skillstormproject1.batstats.models.Warehouse;
import com.skillstormproject1.batstats.repositories.WarehouseRepository;

@Service
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    public WarehouseService(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    // list of all warehouses
    public List<Warehouse> getAllWarehouses(){
        return warehouseRepository.findAll();
    }

    // find a warehouse by id or throw exception
    public Warehouse getWarehouseById(int id){
        return warehouseRepository.findById(id).orElseThrow();
    }

    // list warehouses by status
    public List<Warehouse> getWarehouseByStatus(String status){
        return warehouseRepository.findByStatus(status);
    }

    /**
     * createWarehouse - warehouseDTO
     * updateWarehouse - warehouseDTO
     * deleteWarehouse - id and exception
     */
}
