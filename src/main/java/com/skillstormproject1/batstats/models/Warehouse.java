package com.skillstormproject1.batstats.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "warehouses")
public class Warehouse {
   
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 200)
    private String location;

    @Column(name="max_capacity", nullable = false)
    private Integer maxCapacity;

    @Column(name="current_capacity", nullable = false)
    private Integer currentCapacity = 0;
    
    @Column(length = 20)
    private String status = "ACTIVE";

    @Column(name="created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name="updated_at")
    private LocalDateTime updatedAt;

    // Many-to-many relationship through junction table
    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WarehouseInventory> inventoryItems = new ArrayList<>();
    

    // Default Constructor
    public Warehouse() {
    }

    public Warehouse(String name, String location, Integer maxCapacity) {
        this.name = name;
        this.location = location;
        this.maxCapacity = maxCapacity;
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

    // calculation for the capacity left in warehouse
    public Integer getAvailableCapacity() {
        return maxCapacity - currentCapacity;
    }
    
    // calculation for the percentage of the warehouse used 
    public Double getCapacityPercentage() {
        if (maxCapacity == 0) {
            return 0.0;
        }
        return (currentCapacity.doubleValue() / maxCapacity.doubleValue()) * 100;
    }

    // check if warehouse has enough space to do the request
    public boolean hasCapacity(Integer requiredSpace) {
        return getAvailableCapacity() >= requiredSpace;
    }

    // calculate if the warehouse is running out of space to show and alert
    public boolean isNearCapacity(Double thresholdPercent) {
        return getCapacityPercentage() >= thresholdPercent;
    }

    // get number of unique items in this warehouse
    public Integer getUniqueItemCount() {
        return inventoryItems.size();
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(Integer maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public Integer getCurrentCapacity() {
        return currentCapacity;
    }

    public void setCurrentCapacity(Integer currentCapacity) {
        this.currentCapacity = currentCapacity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public List<WarehouseInventory> getInventoryItems() {
        return inventoryItems;
    }

    public void setInventoryItems(List<WarehouseInventory> inventoryItems) {
        this.inventoryItems = inventoryItems;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((location == null) ? 0 : location.hashCode());
        result = prime * result + ((maxCapacity == null) ? 0 : maxCapacity.hashCode());
        result = prime * result + ((currentCapacity == null) ? 0 : currentCapacity.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((createdAt == null) ? 0 : createdAt.hashCode());
        result = prime * result + ((updatedAt == null) ? 0 : updatedAt.hashCode());
        result = prime * result + ((inventoryItems == null) ? 0 : inventoryItems.hashCode());
        return result;
    }

    // Hash code and Equals
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Warehouse other = (Warehouse) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (location == null) {
            if (other.location != null)
                return false;
        } else if (!location.equals(other.location))
            return false;
        if (maxCapacity == null) {
            if (other.maxCapacity != null)
                return false;
        } else if (!maxCapacity.equals(other.maxCapacity))
            return false;
        if (currentCapacity == null) {
            if (other.currentCapacity != null)
                return false;
        } else if (!currentCapacity.equals(other.currentCapacity))
            return false;
        if (status == null) {
            if (other.status != null)
                return false;
        } else if (!status.equals(other.status))
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
        if (inventoryItems == null) {
            if (other.inventoryItems != null)
                return false;
        } else if (!inventoryItems.equals(other.inventoryItems))
            return false;
        return true;
    }

    // ToString
    @Override
    public String toString() {
        return "Warehouse [id=" + id + ", name=" + name + ", location=" + location + ", maxCapacity=" + maxCapacity
                + ", currentCapacity=" + currentCapacity + ", status=" + status + ", createdAt=" + createdAt
                + ", updatedAt=" + updatedAt + ", inventoryItems=" + inventoryItems + "]";
    }


}
