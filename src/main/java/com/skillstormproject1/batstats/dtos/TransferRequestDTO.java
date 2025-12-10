package com.skillstormproject1.batstats.dtos;

public class TransferRequestDTO {
    
    private Integer itemId;
    private Integer sourceWarehouseId;
    private Integer destinationWarehouseId;
    private Integer quantity;
   
    public TransferRequestDTO() {
    }

    public TransferRequestDTO(Integer itemId, Integer sourceWarehouseId, Integer destinationWarehouseId, Integer quantity) {
        this.itemId = itemId;
        this.sourceWarehouseId = sourceWarehouseId;
        this.destinationWarehouseId = destinationWarehouseId;
        this.quantity = quantity;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Integer getSourceWarehouseId() {
        return sourceWarehouseId;
    }

    public void setSourceWarehouseId(Integer sourceWarehouseId) {
        this.sourceWarehouseId = sourceWarehouseId;
    }

    public Integer getDestinationWarehouseId() {
        return destinationWarehouseId;
    }

    public void setDestinationWarehouseId(Integer destinationWarehouseId) {
        this.destinationWarehouseId = destinationWarehouseId;
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
        result = prime * result + itemId;
        result = prime * result + sourceWarehouseId;
        result = prime * result + destinationWarehouseId;
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
        TransferRequestDTO other = (TransferRequestDTO) obj;
        if (itemId != other.itemId)
            return false;
        if (sourceWarehouseId != other.sourceWarehouseId)
            return false;
        if (destinationWarehouseId != other.destinationWarehouseId)
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
        return "TransferRequestDTO [itemId=" + itemId + ", sourceWarehouseId=" + sourceWarehouseId
                + ", destinationWarehouseId=" + destinationWarehouseId + ", quantity=" + quantity + "]";
    }

    
}
