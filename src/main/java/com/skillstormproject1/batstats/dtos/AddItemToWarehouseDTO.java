package com.skillstormproject1.batstats.dtos;

public class AddItemToWarehouseDTO {

    private Integer inventoryItemId;
    private Integer warehouseId;
    private Integer quantity;
    
    public AddItemToWarehouseDTO() {
    }

    public AddItemToWarehouseDTO(Integer inventoryItemId, Integer warehouseId, Integer quantity) {
        this.inventoryItemId = inventoryItemId;
        this.warehouseId = warehouseId;
        this.quantity = quantity;
    }

    public Integer getInventoryItemId() {
        return inventoryItemId;
    }

    public void setInventoryItemId(Integer inventoryItemId) {
        this.inventoryItemId = inventoryItemId;
    }

    public Integer getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Integer warehouseId) {
        this.warehouseId = warehouseId;
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
        result = prime * result + ((inventoryItemId == null) ? 0 : inventoryItemId.hashCode());
        result = prime * result + ((warehouseId == null) ? 0 : warehouseId.hashCode());
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
        AddItemToWarehouseDTO other = (AddItemToWarehouseDTO) obj;
        if (inventoryItemId == null) {
            if (other.inventoryItemId != null)
                return false;
        } else if (!inventoryItemId.equals(other.inventoryItemId))
            return false;
        if (warehouseId == null) {
            if (other.warehouseId != null)
                return false;
        } else if (!warehouseId.equals(other.warehouseId))
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
        return "AddItemToWarehouseDTO [inventoryItemId=" + inventoryItemId + ", warehouseId=" + warehouseId
                + ", quantity=" + quantity + "]";
    }

}
