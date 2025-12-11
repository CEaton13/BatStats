package com.skillstormproject1.batstats.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "inventory_items")
public class InventoryItem {
    
    // map all of these to columns
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name="serial_number", nullable = false, unique = true, length = 50)
    private String serialNumber;

    // productType is where the name of item is located
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_type_id", nullable = false)
    private ProductType productType;

    // Many-to-many relationship through junction table
    @OneToMany(mappedBy = "inventoryItem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<WarehouseInventory> warehouseLocations = new ArrayList<>(); 
    
    @Column(name="created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name="updated_at")
    private LocalDateTime updatedAt;

    public InventoryItem() {
    }

    public InventoryItem(String serialNumber, ProductType productType) {
        this.serialNumber = serialNumber;
        this.productType = productType;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // methods to handle quantity across warehouses and locations

    // get total quantity across all warehouse locations
    @JsonProperty("totalQuantity")
     public Integer getTotalQuantity() {
        if (warehouseLocations == null || warehouseLocations.isEmpty()) {
            return 0;
        }
        return warehouseLocations.stream()
                .mapToInt(WarehouseInventory::getQuantity)
                .sum();
    }

    // get number of warehouse locations storing this item
    @JsonProperty("locationCount")
    public Integer getLocationCount() {
        if (warehouseLocations == null) {
            return 0;
        }
        return warehouseLocations.size();
    }

    // add this item to a warehouse location
    public void addToWarehouse(Warehouse warehouse, Integer quantity) {
        WarehouseInventory location = new WarehouseInventory(warehouse, this, quantity);
        warehouseLocations.add(location);
    }

    // remove this item from a warehouse location
    public void removeFromWarehouse(Warehouse warehouse) {
        warehouseLocations.removeIf(loc -> loc.getWarehouse().equals(warehouse));
    }

    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getSerialNumber() {
        return serialNumber;
    }
    
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }
    
    public ProductType getProductType() {
        return productType;
    }
    
    public void setProductType(ProductType productType) {
        this.productType = productType;
    }
    
    public List<WarehouseInventory> getWarehouseLocations() {
        return warehouseLocations;
    }
    
    public void setWarehouseLocations(List<WarehouseInventory> warehouseLocations) {
        this.warehouseLocations = warehouseLocations;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((serialNumber == null) ? 0 : serialNumber.hashCode());
        result = prime * result + ((productType == null) ? 0 : productType.hashCode());
        result = prime * result + ((warehouseLocations == null) ? 0 : warehouseLocations.hashCode());
        result = prime * result + ((createdAt == null) ? 0 : createdAt.hashCode());
        result = prime * result + ((updatedAt == null) ? 0 : updatedAt.hashCode());
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
        InventoryItem other = (InventoryItem) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (serialNumber == null) {
            if (other.serialNumber != null)
                return false;
        } else if (!serialNumber.equals(other.serialNumber))
            return false;
        if (productType == null) {
            if (other.productType != null)
                return false;
        } else if (!productType.equals(other.productType))
            return false;
        if (warehouseLocations == null) {
            if (other.warehouseLocations != null)
                return false;
        } else if (!warehouseLocations.equals(other.warehouseLocations))
            return false;
        if (createdAt == null) {
            if (other.createdAt != null)
                return false;
        } else if (!createdAt.equals(other.createdAt))
            return false;
        if (updatedAt == null) {
            if (other.updatedAt != null)
                return false;
        } else if (!updatedAt.equals(other.updatedAt))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "InventoryItem [id=" + id + ", serialNumber=" + serialNumber + ", productType=" + productType
                + ", warehouseLocations=" + warehouseLocations + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt
                + "]";
    }

   

}
