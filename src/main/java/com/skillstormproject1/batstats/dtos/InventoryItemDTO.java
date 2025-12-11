package com.skillstormproject1.batstats.dtos;

import java.util.ArrayList;
import java.util.List;

public class InventoryItemDTO {
    
    private String serialNumber;
    private Integer productTypeId;

    // list the warehouse locations get mapping
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
        if (locations == null) {
            if (other.locations != null)
                return false;
        } else if (!locations.equals(other.locations))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "InventoryItemDTO [serialNumber=" + serialNumber + ", productTypeId=" + productTypeId + ", locations="
                + locations + "]";
    }

}
