package com.skillstormproject1.batstats.dtos;


public class InventoryItemDTO {
    
    private String serialNumber;
    private Integer productTypeId;
    private Integer warehouseId;
    private Integer quantity;
    
    public InventoryItemDTO() {
    }

    public InventoryItemDTO(String serialNumber, Integer productTypeId, Integer warehouseId, Integer quantity) {
        this.serialNumber = serialNumber;
        this.productTypeId = productTypeId;
        this.warehouseId = warehouseId;
        this.quantity = quantity;
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
        result = prime * result + ((serialNumber == null) ? 0 : serialNumber.hashCode());
        result = prime * result + productTypeId;
        result = prime * result + warehouseId;
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
        InventoryItemDTO other = (InventoryItemDTO) obj;
        if (serialNumber == null) {
            if (other.serialNumber != null)
                return false;
        } else if (!serialNumber.equals(other.serialNumber))
            return false;
        if (productTypeId != other.productTypeId)
            return false;
        if (warehouseId != other.warehouseId)
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
        return "InventoryItemDTO [serialNumber=" + serialNumber + ", productTypeId=" + productTypeId + ", warehouseId="
                + warehouseId + ", quantity=" + quantity + "]";
    }
}
