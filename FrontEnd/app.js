// API Base URL
const API_BASE_URL = 'http://localhost:8080/api';

let currentSection = 'dashboard';
let warehouses = [];
let inventoryItems = [];
let productTypes = [];

// Initialize application
document.addEventListener('DOMContentLoaded', function() {
    console.log('BatStats Inventory System initialized');
    setupNavigation();
    loadDashboard();
    //setupForms();
});


// nav links setup for on click 
function setupNavigation() {
    const navLinks = document.querySelectorAll('.nav-link[data-section]');
    
    navLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            
            // Update active state
            navLinks.forEach(l => l.classList.remove('active'));
            this.classList.add('active');
            
            // Show selected section
            const section = this.dataset.section;
            showSection(section);
        });
    });
}


function showSection(sectionName) {
    currentSection = sectionName;
    
    // Hide all sections
    document.querySelectorAll('.content-section').forEach(section => {
        section.style.display = 'none';
    });
    
    // Show selected section
    const section = document.getElementById(`${sectionName}-section`);
    if (section) {
        section.style.display = 'block';
    }
    
    // Load section data
    switch(sectionName) {
        case 'dashboard':
            loadDashboard();
            break;
        case 'warehouses':
            loadWarehouses();
            break;
        case 'inventory':
            loadInventoryItems();
            populateWarehouseSelects();
            populateProductTypeSelects();
            break;
        case 'products':
            loadProductTypes();
            break;
    }
}

// setting up forms to submit
function setupForms() {
    // Warehouse form
    document.getElementById('warehouseForm').addEventListener('submit', async function(e) {
        e.preventDefault();
        await saveWarehouse();
    });
    
    // Inventory form
    document.getElementById('inventoryForm').addEventListener('submit', async function(e) {
        e.preventDefault();
        await saveInventoryItem();
    });
    
    // Product form
    document.getElementById('productForm').addEventListener('submit', async function(e) {
        e.preventDefault();
        await saveProductType();
    });
}


// DASHBOARD

async function loadDashboard() {
    try {
        await Promise.all([
            loadWarehouses(),
            //loadInventoryItems(),
           // loadProductTypes()
        ]);
        
        updateDashboardStats();
        displayWarehouseCapacity();
        //displayAlerts();
    } catch (error) {
        console.error('Error loading dashboard:', error);
        showAlert('Failed to load dashboard data', 'danger');
    }
}

function updateDashboardStats() {
    document.getElementById('totalWarehouses').textContent = warehouses.length;
    document.getElementById('totalItems').textContent = inventoryItems.length;
    document.getElementById('totalProductTypes').textContent = productTypes.length;
    
    if (warehouses.length > 0) {
        const avgCapacity = warehouses.reduce((sum, w) => sum + w.capacityPercentage, 0) / warehouses.length;
        document.getElementById('avgCapacity').textContent = avgCapacity.toFixed(1) + '%';
    }
}

function displayWarehouseCapacity() {
    const container = document.getElementById('warehouseCapacityList');
    
    if (warehouses.length === 0) {
        container.innerHTML = '<p class="text-grey">No warehouses available</p>';
        return;
    }
    
    let html = '';
    warehouses.forEach(warehouse => {
        const percentage = warehouse.capacityPercentage;
        const progressClass = percentage >= 90 ? 'danger' : percentage >= 75 ? 'warning' : 'success';
        
        html += `
            <div class="mb-3">
                <div class="d-flex justify-content-between mb-2">
                    <strong class="text-yellow">${warehouse.name}</strong>
                    <span class="text-grey">${warehouse.currentCapacity} / ${warehouse.maxCapacity}</span>
                </div>
                <div class="progress">
                    <div class="progress-bar bg-${progressClass}" role="progressbar" 
                         style="width: ${percentage}%">
                        ${percentage.toFixed(1)}%
                    </div>
                </div>
            </div>
        `;
    });
    
    container.innerHTML = html;
}



// Warehouses
async function loadWarehouses() {
    try {
        const response = await fetch(`${API_BASE_URL}/warehouses`);
        warehouses = await response.json();
        
        if (currentSection === 'warehouses') {
            displayWarehouses();
        }
        
        populateWarehouseSelects();
        return warehouses;
    } catch (error) {
        console.error('Error loading warehouses:', error);
        throw error;
    }
}

function displayWarehouses() {
    const tbody = document.getElementById('warehousesTableBody');
    
    if (warehouses.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center text-grey">No warehouses found</td></tr>';
        return;
    }
    
    let html = '';
    warehouses.forEach(warehouse => {
        const statusBadge = warehouse.status === 'ACTIVE' ? 
            '<span class="badge badge-success">Active</span>' : 
            '<span class="badge badge-danger">Inactive</span>';
        
        const percentage = warehouse.capacityPercentage;
        const progressClass = percentage >= 90 ? 'danger' : percentage >= 75 ? 'warning' : 'success';
        
        html += `
            <tr>
                <td>${warehouse.id}</td>
                <td><strong class="text-yellow">${warehouse.name}</strong></td>
                <td>${warehouse.location}</td>
                <td>${warehouse.currentCapacity} / ${warehouse.maxCapacity}</td>
                <td>
                    <div class="progress" style="height: 25px;">
                        <div class="progress-bar bg-${progressClass}" style="width: ${percentage}%">
                            ${percentage.toFixed(0)}%
                        </div>
                    </div>
                </td>
                <td>${statusBadge}</td>
                <td>
                    <button class="btn btn-bat-action btn-sm" onclick="editWarehouse(${warehouse.id})">
                        <i class="bi bi-pencil"></i> Edit
                    </button>
                    <button class="btn btn-bat-danger btn-sm" onclick="deleteWarehouse(${warehouse.id})">
                        <i class="bi bi-trash"></i> Delete
                    </button>
                </td>
            </tr>
        `;
    });
    
    tbody.innerHTML = html;
}

// creating a new warhouse function
async function saveWarehouse() {
    const id = document.getElementById('warehouseId').value;
    const warehouse = {
        name: document.getElementById('warehouseName').value,
        location: document.getElementById('warehouseLocation').value,
        maxCapacity: parseInt(document.getElementById('warehouseCapacity').value),
        status: document.getElementById('warehouseStatus').value
    };
    
    try {
        const url = id ? `${API_BASE_URL}/warehouses/${id}` : `${API_BASE_URL}/warehouses`;
        const method = id ? 'PUT' : 'POST';
        
        const response = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(warehouse)
        });
        
        if (!response.ok) throw new Error('Failed to save warehouse');
        
        // Clear form
        document.getElementById('warehouseForm').reset();
        document.getElementById('warehouseId').value = '';
        
        await loadWarehouses();
        showAlert(`Warehouse ${id ? 'updated' : 'created'} successfully`, 'success');
    } catch (error) {
        console.error('Error saving warehouse:', error);
        showAlert('Failed to save warehouse', 'danger');
    }
}
// edit function for a warehouse 
function editWarehouse(id) {
    const warehouse = warehouses.find(w => w.id === id);
    if (!warehouse) return;
    
    document.getElementById('warehouseId').value = warehouse.id;
    document.getElementById('warehouseName').value = warehouse.name;
    document.getElementById('warehouseLocation').value = warehouse.location;
    document.getElementById('warehouseCapacity').value = warehouse.maxCapacity;
    document.getElementById('warehouseStatus').value = warehouse.status;
    
    // Scroll to form
    document.getElementById('warehouseForm').scrollIntoView({ behavior: 'smooth' });
}

// deleting a warehouse from the database
async function deleteWarehouse(id) {
    if (!confirm('Are you sure you want to delete this warehouse?')) return;
    
    try {
        const response = await fetch(`${API_BASE_URL}/warehouses/${id}`, {
            method: 'DELETE'
        });
        
        if (!response.ok) {
            const error = await response.text();
            throw new Error(error);
        }
        
        await loadWarehouses();
        showAlert('Warehouse deleted successfully', 'success');
    } catch (error) {
        console.error('Error deleting warehouse:', error);
        showAlert(error.message, 'danger');
    }
}


// Inventory Items
async function loadInventoryItems() {
    try {
        const response = await fetch(`${API_BASE_URL}/inventory`);
        inventoryItems = await response.json();
        
        if (currentSection === 'inventory') {
            displayInventoryItems();
        }
        
        return inventoryItems;
    } catch (error) {
        console.error('Error loading inventory items:', error);
        throw error;
    }
}

function displayInventoryItems() {
    const tbody = document.getElementById('inventoryTableBody');
    
    if (inventoryItems.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center text-grey">No inventory items found</td></tr>';
        return;
    }
    
    let html = '';
    inventoryItems.forEach(item => {
        const conditionBadge = getConditionBadge(item.condition);
        
        html += `
            <tr>
                <td><code class="text-yellow">${item.serialNumber}</code></td>
                <td>${item.productType.name}</td>
                <td>${item.warehouse.name}</td>
                <td><span class="badge badge-blue">${item.quantity}</span></td>
                <td>
                    <button class="btn btn-bat-action btn-sm" onclick="editInventoryItem(${item.id})">
                        <i class="bi bi-pencil"></i> Edit
                    </button>
                    <button class="btn btn-bat-danger btn-sm" onclick="deleteInventoryItem(${item.id})">
                        <i class="bi bi-trash"></i> Delete
                    </button>
                </td>
            </tr>
        `;
    });
    
    tbody.innerHTML = html;
}

// create a new Inventory Item in the database
async function saveInventoryItem() {
    const id = document.getElementById('inventoryId').value;
    const item = {
        serialNumber: document.getElementById('inventorySerial').value,
        productType: { id: parseInt(document.getElementById('inventoryProductType').value) },
        warehouse: { id: parseInt(document.getElementById('inventoryWarehouse').value) },
        quantity: parseInt(document.getElementById('inventoryQuantity').value),
    };
    
    try {
        const url = id ? `${API_BASE_URL}/inventory/${id}` : `${API_BASE_URL}/inventory`;
        const method = id ? 'PUT' : 'POST';
        
        const response = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(item)
        });
        
        if (!response.ok) {
            const error = await response.text();
            throw new Error(error);
        }
        
        // Clear form
        document.getElementById('inventoryForm').reset();
        document.getElementById('inventoryId').value = '';
        
        await loadInventoryItems();
        await loadWarehouses();
        showAlert(`Inventory item ${id ? 'updated' : 'created'} successfully`, 'success');
    } catch (error) {
        console.error('Error saving inventory item:', error);
        showAlert(error.message, 'danger');
    }
}
// update inventory item
function editInventoryItem(id) {
    const item = inventoryItems.find(i => i.id === id);
    if (!item) return;
    
    document.getElementById('inventoryId').value = item.id;
    document.getElementById('inventorySerial').value = item.serialNumber;
    document.getElementById('inventoryProductType').value = item.productType.id;
    document.getElementById('inventoryWarehouse').value = item.warehouse.id;
    document.getElementById('inventoryQuantity').value = item.quantity;
    
    // Scroll to form
    document.getElementById('inventoryForm').scrollIntoView({ behavior: 'smooth' });
}
// delete inventory item
async function deleteInventoryItem(id) {
    if (!confirm('Are you sure you want to delete this inventory item?')) return;
    
    try {
        const response = await fetch(`${API_BASE_URL}/inventory/${id}`, {
            method: 'DELETE'
        });
        
        if (!response.ok) throw new Error('Failed to delete inventory item');
        
        await loadInventoryItems();
        await loadWarehouses();
        showAlert('Inventory item deleted successfully', 'success');
    } catch (error) {
        console.error('Error deleting inventory item:', error);
        showAlert(error.message, 'danger');
    }
}


// Product Type 