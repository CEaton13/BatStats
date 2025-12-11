package com.skillstormproject1.batstats.dtos;

public class WarehouseLocationDTO {

    private Integer warehouseId;
    private String warehouseName;
    private String warehouseLocation;
    private Integer quantity;
    
    public WarehouseLocationDTO() {
    }

    public WarehouseLocationDTO(Integer warehouseId, String warehouseName, String warehouseLocation, Integer quantity) {
        this.warehouseId = warehouseId;
        this.warehouseName = warehouseName;
        this.warehouseLocation = warehouseLocation;
        this.quantity = quantity;
    }

    public Integer getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Integer warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    public String getWarehouseLocation() {
        return warehouseLocation;
    }

    public void setWarehouseLocation(String warehouseLocation) {
        this.warehouseLocation = warehouseLocation;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((warehouseId == null) ? 0 : warehouseId.hashCode());
        result = prime * result + ((warehouseName == null) ? 0 : warehouseName.hashCode());
        result = prime * result + ((warehouseLocation == null) ? 0 : warehouseLocation.hashCode());
        result = prime * result + ((quantity == null) ? 0 : quantity.hashCode());
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
        WarehouseLocationDTO other = (WarehouseLocationDTO) obj;
        if (warehouseId == null) {
            if (other.warehouseId != null)
                return false;
        } else if (!warehouseId.equals(other.warehouseId))
            return false;
        if (warehouseName == null) {
            if (other.warehouseName != null)
                return false;
        } else if (!warehouseName.equals(other.warehouseName))
            return false;
        if (warehouseLocation == null) {
            if (other.warehouseLocation != null)
                return false;
        } else if (!warehouseLocation.equals(other.warehouseLocation))
            return false;
        if (quantity == null) {
            if (other.quantity != null)
                return false;
        } else if (!quantity.equals(other.quantity))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "WarehouseLocationDTO [warehouseId=" + warehouseId + ", warehouseName=" + warehouseName
                + ", warehouseLocation=" + warehouseLocation + ", quantity=" + quantity + "]";
    }

}
