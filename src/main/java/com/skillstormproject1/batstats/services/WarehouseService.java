package com.skillstormproject1.batstats.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.skillstormproject1.batstats.dtos.WarehouseDTO;
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
    public Warehouse getWarehouseById(int id) {
        return warehouseRepository.findById(id).orElseThrow(() -> 
        new ResponseStatusException(HttpStatus.NOT_FOUND, "Warehouse not found with id: " + id));
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

    public Warehouse createWarehouse(WarehouseDTO warehouseDTO) {
        Warehouse warehouse = new Warehouse();
        warehouse.setName(warehouseDTO.getName());
        warehouse.setLocation(warehouseDTO.getLocation());
        warehouse.setMaxCapacity(warehouseDTO.getMaxCapacity());
        warehouse.setStatus(warehouseDTO.getStatus() != null ? warehouseDTO.getStatus() : "ACTIVE");
        warehouse.setCurrentCapacity(0);
        return warehouseRepository.save(warehouse);
    }

    public Warehouse updateWarehouse(int id, WarehouseDTO warehouseDTO) {
        Warehouse existing = getWarehouseById(id);
        existing.setName(warehouseDTO.getName());
        existing.setLocation(warehouseDTO.getLocation());
        existing.setMaxCapacity(warehouseDTO.getMaxCapacity());
        if (warehouseDTO.getStatus() != null) {
            existing.setStatus(warehouseDTO.getStatus());
        }
        return warehouseRepository.save(existing);
    }

    public void deleteWarehouse(int id) {
        if (!warehouseRepository.existsById(id)) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,"Warehouse not found with id: " + id);
        }
        warehouseRepository.deleteById(id);
    }
}
