package com.skillstormproject1.batstats.dtos;

import java.util.ArrayList;
import java.util.List;

public class InventoryItemDTO {
    
    private String serialNumber;  // Can be null for auto-generation
    private Integer productTypeId;
    
    // Initial warehouse assignment (for creation)
    private Integer initialWarehouseId;
    private Integer initialQuantity;

    // List of warehouse locations (for GET mapping)
    private List<WarehouseLocationDTO> locations = new ArrayList<>();
    
    public InventoryItemDTO() {
    }

    public InventoryItemDTO(String serialNumber, Integer productTypeId) {
        this.serialNumber = serialNumber;
        this.productTypeId = productTypeId;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public Integer getProductTypeId() {
        return productTypeId;
    }

    public void setProductTypeId(Integer productTypeId) {
        this.productTypeId = productTypeId;
    }

    public Integer getInitialWarehouseId() {
        return initialWarehouseId;
    }

    public void setInitialWarehouseId(Integer initialWarehouseId) {
        this.initialWarehouseId = initialWarehouseId;
    }

    public Integer getInitialQuantity() {
        return initialQuantity;
    }

    public void setInitialQuantity(Integer initialQuantity) {
        this.initialQuantity = initialQuantity;
    }

    public List<WarehouseLocationDTO> getLocations() {
        return locations;
    }

    public void setLocations(List<WarehouseLocationDTO> locations) {
        this.locations = locations;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((serialNumber == null) ? 0 : serialNumber.hashCode());
        result = prime * result + ((productTypeId == null) ? 0 : productTypeId.hashCode());
        result = prime * result + ((initialWarehouseId == null) ? 0 : initialWarehouseId.hashCode());
        result = prime * result + ((initialQuantity == null) ? 0 : initialQuantity.hashCode());
        result = prime * result + ((locations == null) ? 0 : locations.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        InventoryItemDTO other = (InventoryItemDTO) obj;
        if (serialNumber == null) {
            if (other.serialNumber != null)
                return false;
        } else if (!serialNumber.equals(other.serialNumber))
            return false;
        if (productTypeId == null) {
            if (other.productTypeId != null)
                return false;
        } else if (!productTypeId.equals(other.productTypeId))
            return false;
        if (initialWarehouseId == null) {
            if (other.initialWarehouseId != null)
                return false;
        } else if (!initialWarehouseId.equals(other.initialWarehouseId))
            return false;
        if (initialQuantity == null) {
            if (other.initialQuantity != null)
                return false;
        } else if (!initialQuantity.equals(other.initialQuantity))
            return false;
        if (locations == null) {
            if (other.locations != null)
                return false;
        } else if (!locations.equals(other.locations))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "InventoryItemDTO [serialNumber=" + serialNumber + ", productTypeId=" + productTypeId 
                + ", initialWarehouseId=" + initialWarehouseId + ", initialQuantity=" + initialQuantity 
                + ", locations=" + locations + "]";
    }
    
}
