// API Base URL
const API_BASE_URL = 'http://localhost:8080/api';

let currentSection = 'dashboard';
let warehouses = [];
let inventoryItems = [];
let allInventoryItems = [];
let productTypes = [];
let currentLocationItem = null;  // NEW: Track current item in locations modal

// Initialize application
document.addEventListener('DOMContentLoaded', function() {
    console.log('BatStats Inventory System initialized');
    setupNavigation();
    loadDashboard();
    setupForms();
    setupSearchAndFilter();
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
            populateFilters();
            break;
        case 'products':
            loadProductTypes();
            break;
    }
}

// setting up forms to submit
function setupForms() {
    // Warehouse form
    const warehouseForm = document.getElementById('warehouseForm');
    if (warehouseForm) {
        warehouseForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            await saveWarehouse();
        });
    }
    
    // Inventory form
    const inventoryForm = document.getElementById('inventoryForm');
    if (inventoryForm) {
        inventoryForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            await saveInventoryItem();
        });
    }
    // Product form
    const productForm = document.getElementById('productForm');
    if (productForm) {
        productForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            await saveProductType();
        });
    }
    // Transfer Request Form
    const transferForm = document.getElementById('transferForm');
    if (transferForm) {
        transferForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            await transferInventoryItem();
        });
    }
    
    // NEW: Location management form
    const addLocationForm = document.getElementById('addLocationForm');
    if (addLocationForm) {
        addLocationForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            await addItemToWarehouseLocation();
        });
    }
    
    // NEW: Modal transfer form
    const modalTransferForm = document.getElementById('modalTransferForm');
    if (modalTransferForm) {
        modalTransferForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            await transferFromModal();
        });
    }
}

// Alerts to show if functions properly updated or failed
function showAlert(message, type = 'info') {
    const alertHTML = `
        <div class="alert alert-${type} alert-dismissible fade show" role="alert">
            <i class="bi bi-${type === 'success' ? 'check-circle' : type === 'danger' ? 'exclamation-circle' : 'info-circle'}"></i>
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `;
    
    const container = document.querySelector('.container');
    const alertDiv = document.createElement('div');
    alertDiv.innerHTML = alertHTML;
    container.insertBefore(alertDiv.firstElementChild, container.firstChild);
    
    // Auto-dismiss after 5 seconds
    setTimeout(() => {
        const alert = container.querySelector('.alert');
        if (alert) alert.remove();
    }, 5000);
}

// getting search filter views setup
function setupSearchAndFilter() {
    // Search input
    const searchInput = document.getElementById('inventorySearch');
    if (searchInput) {
        searchInput.addEventListener('input', function(e) {
            filterInventoryItems();
        });
    }
    
    // Warehouse filter
    const warehouseFilter = document.getElementById('warehouseFilter');
    if (warehouseFilter) {
        warehouseFilter.addEventListener('change', function(e) {
            filterInventoryItems();
        });
    }
    
    // Product type filter
    const productTypeFilter = document.getElementById('productTypeFilter');
    if (productTypeFilter) {
        productTypeFilter.addEventListener('change', function(e) {
            filterInventoryItems();
        });
    }
    
    // Clear filters button
    const clearFiltersBtn = document.getElementById('clearFilters');
    if (clearFiltersBtn) {
        clearFiltersBtn.addEventListener('click', function() {
            document.getElementById('inventorySearch').value = '';
            document.getElementById('filterWarehouse').value = '';
            document.getElementById('filterProduct').value = '';
            filterInventoryItems();
        });
    }
}

// DASHBOARD

async function loadDashboard() {
    try {
        await Promise.all([
            loadWarehouses(),
            loadInventoryItems(),
            loadProductTypes()
        ]);
        
        updateDashboardStats();
        displayWarehouseCapacity();
        displayAlerts();
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

function displayAlerts() {
    const container = document.getElementById('alertsList');
    const criticalWarehouses = warehouses.filter(w => w.capacityPercentage >= 90);
    const warningWarehouses = warehouses.filter(w => w.capacityPercentage >= 75 && w.capacityPercentage < 90);
    
    if (criticalWarehouses.length === 0 && warningWarehouses.length === 0) {
        container.innerHTML = '<p class="text-grey small">No alerts at this time</p>';
        return;
    }
    
    let html = '';
    
    criticalWarehouses.forEach(w => {
        html += `
            <div class="alert alert-danger py-2 px-3 mb-2">
                <small><i class="bi bi-exclamation-triangle-fill"></i> 
                <strong>${w.name}</strong> at ${w.capacityPercentage.toFixed(1)}%</small>
            </div>
        `;
    });
    
    warningWarehouses.forEach(w => {
        html += `
            <div class="alert alert-warning py-2 px-3 mb-2">
                <small><i class="bi bi-exclamation-circle-fill"></i> 
                <strong>${w.name}</strong> at ${w.capacityPercentage.toFixed(1)}%</small>
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
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        allInventoryItems = await response.json();
        inventoryItems = [...allInventoryItems];

        console.log('Loaded inventory items:', inventoryItems); // Debug log
        
        if (currentSection === 'inventory') {
            displayInventoryItems();
        }
        
        return inventoryItems;
    } catch (error) {
        console.error('Error loading inventory items:', error);
        showAlert('Failed to load inventory items', 'danger');
        throw error;
    }
}

function displayInventoryItems() {
    const tbody = document.getElementById('inventoryTableBody');
    
    if (!inventoryItems || inventoryItems.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center text-grey">No inventory items found</td></tr>';
        return;
    }
    
    let html = '';
    inventoryItems.forEach(item => {
        // Defensive checks for all nested properties
        const serialNumber = item.serialNumber || 'N/A';
        const productName = item.productType?.name || 'Unknown Product';
        const locations = item.warehouseLocations || [];
        
        // Display multiple locations
        let locationsHtml = '<span class="text-grey">No locations</span>';
        if (locations.length > 0) {
            locationsHtml = locations
                .map(loc => {
                    const warehouseName = loc.warehouse?.name || 'Unknown';
                    const quantity = loc.quantity || 0;
                    return `<span class="badge badge-blue">${warehouseName}: ${quantity}</span>`;
                })
                .join(' ');
        }
        
        // Calculate total quantity safely
        const totalQuantity = locations.reduce((sum, loc) => sum + (loc.quantity || 0), 0);
        
        html += `
            <tr>
                <td><code class="text-yellow">${serialNumber}</code></td>
                <td>${productName}</td>
                <td>${locationsHtml}</td>
                <td><span class="badge badge-yellow">${totalQuantity}</span></td>
                <td>${locations.length} location${locations.length !== 1 ? 's' : ''}</td>
                <td>
                    <button class="btn btn-bat-action btn-sm" onclick="manageItemLocations(${item.id})">
                        <i class="bi bi-pin-map"></i> Locations
                    </button>
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

// NEW: Complete implementation of warehouse locations management
async function manageItemLocations(itemId) {
    const item = inventoryItems.find(i => i.id === itemId);
    if (!item) {
        showAlert('Item not found', 'danger');
        return;
    }
    
    currentLocationItem = item;
    
    try {
        // Fetch current locations from backend
        const response = await fetch(`${API_BASE_URL}/warehouse-inventory/item/${itemId}`);
        if (!response.ok) throw new Error('Failed to load locations');
        
        const locations = await response.json();
        
        // Update modal with item details
        document.getElementById('modalItemSerial').textContent = item.serialNumber;
        document.getElementById('modalItemProduct').textContent = item.productType?.name || 'Unknown';
        
        const totalQty = locations.reduce((sum, loc) => sum + (loc.quantity || 0), 0);
        document.getElementById('modalItemTotalQty').textContent = totalQty;
        document.getElementById('modalItemLocationCount').textContent = locations.length;
        
        // Set hidden item IDs
        document.getElementById('addLocationItemId').value = itemId;
        document.getElementById('modalTransferItemId').value = itemId;
        
        // Display current locations
        displayCurrentLocations(locations);
        
        // Populate warehouse selects
        populateModalWarehouseSelects(locations);
        
        // Show the modal
        const modal = new bootstrap.Modal(document.getElementById('locationManagementModal'));
        modal.show();
        
    } catch (error) {
        console.error('Error loading locations:', error);
        showAlert('Failed to load warehouse locations', 'danger');
    }
}

function displayCurrentLocations(locations) {
    const container = document.getElementById('currentLocationsList');
    
    if (!locations || locations.length === 0) {
        container.innerHTML = '<p class="text-grey small">This item is not currently stored in any warehouse.</p>';
        return;
    }
    
    let html = '';
    locations.forEach(location => {
        const warehouse = location.warehouse || {};
        const warehouseName = warehouse.name || 'Unknown Warehouse';
        const warehouseLocation = warehouse.location || 'Unknown Location';
        const quantity = location.quantity || 0;
        const locationId = location.id;
        
        html += `
            <div class="card bg-secondary text-light mb-2">
                <div class="card-body p-3">
                    <div class="row align-items-center">
                        <div class="col-md-4">
                            <strong class="text-yellow">${warehouseName}</strong><br>
                            <small class="text-grey">${warehouseLocation}</small>
                        </div>
                        <div class="col-md-3">
                            <small class="text-grey">Quantity:</small><br>
                            <span class="badge badge-yellow">${quantity}</span>
                        </div>
                        <div class="col-md-5 text-end">
                            <div class="input-group input-group-sm d-inline-flex" style="width: auto;">
                                <input type="number" class="form-control form-control-sm" 
                                       id="qty-${locationId}" value="${quantity}" min="1" style="width: 80px;">
                                <button class="btn btn-bat-action btn-sm" onclick="updateLocationQuantity(${locationId})">
                                    <i class="bi bi-check"></i> Update
                                </button>
                            </div>
                            <button class="btn btn-bat-danger btn-sm ms-2" onclick="removeFromLocation(${warehouse.id}, ${currentLocationItem.id}, '${warehouseName}')">
                                <i class="bi bi-trash"></i> Remove
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;
    });
    
    container.innerHTML = html;
}

function populateModalWarehouseSelects(currentLocations) {
    // Get warehouses that DON'T currently have this item (for adding)
    const currentWarehouseIds = currentLocations.map(loc => loc.warehouse?.id);
    const availableWarehouses = warehouses.filter(w => 
        !currentWarehouseIds.includes(w.id) && w.status === 'ACTIVE'
    );
    
    // Populate "Add to Warehouse" select
    const addSelect = document.getElementById('addLocationWarehouse');
    addSelect.innerHTML = '<option value="">Choose warehouse...</option>';
    availableWarehouses.forEach(warehouse => {
        const option = document.createElement('option');
        option.value = warehouse.id;
        option.textContent = `${warehouse.name} (Available: ${warehouse.availableCapacity || 0})`;
        addSelect.appendChild(option);
    });
    
    // Populate transfer source select (warehouses that HAVE this item)
    const sourceSelect = document.getElementById('modalTransferSource');
    sourceSelect.innerHTML = '<option value="">Select source...</option>';
    currentLocations.forEach(location => {
        const warehouse = location.warehouse;
        const option = document.createElement('option');
        option.value = warehouse.id;
        option.textContent = `${warehouse.name} (Qty: ${location.quantity})`;
        option.dataset.quantity = location.quantity;
        sourceSelect.appendChild(option);
    });
    
    // Populate transfer destination select
    const destSelect = document.getElementById('modalTransferDestination');
    destSelect.innerHTML = '<option value="">Select destination...</option>';
    
    // Update destination based on source selection
    sourceSelect.addEventListener('change', function() {
        const sourceId = parseInt(this.value);
        destSelect.innerHTML = '<option value="">Select destination...</option>';
        
        warehouses.forEach(warehouse => {
            if (warehouse.id !== sourceId && warehouse.status === 'ACTIVE') {
                const option = document.createElement('option');
                option.value = warehouse.id;
                option.textContent = `${warehouse.name} (Available: ${warehouse.availableCapacity || 0})`;
                destSelect.appendChild(option);
            }
        });
        
        // Set max quantity for transfer
        const selectedOption = this.options[this.selectedIndex];
        const maxQty = selectedOption.dataset.quantity || 0;
        document.getElementById('modalTransferQuantity').max = maxQty;
    });
}

async function addItemToWarehouseLocation() {
    const itemId = parseInt(document.getElementById('addLocationItemId').value);
    const warehouseId = parseInt(document.getElementById('addLocationWarehouse').value);
    const quantity = parseInt(document.getElementById('addLocationQuantity').value);
    
    if (!warehouseId) {
        showAlert('Please select a warehouse', 'warning');
        return;
    }
    
    const dto = {
        inventoryItemId: itemId,
        warehouseId: warehouseId,
        quantity: quantity
    };
    
    try {
        const response = await fetch(`${API_BASE_URL}/warehouse-inventory`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(dto)
        });
        
        if (!response.ok) {
            const error = await response.text();
            throw new Error(error);
        }
        
        showAlert('Item added to warehouse successfully', 'success');
        
        // Reset form
        document.getElementById('addLocationForm').reset();
        
        // Reload the locations in the modal
        await manageItemLocations(itemId);
        
        // Reload inventory and warehouses data
        await loadInventoryItems();
        await loadWarehouses();
        
    } catch (error) {
        console.error('Error adding item to warehouse:', error);
        showAlert(error.message, 'danger');
    }
}

async function updateLocationQuantity(locationId) {
    const newQuantity = parseInt(document.getElementById(`qty-${locationId}`).value);
    
    if (!newQuantity || newQuantity < 1) {
        showAlert('Please enter a valid quantity', 'warning');
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/warehouse-inventory/${locationId}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ quantity: newQuantity })
        });
        
        if (!response.ok) {
            const error = await response.text();
            throw new Error(error);
        }
        
        showAlert('Quantity updated successfully', 'success');
        
        // Reload the locations in the modal
        await manageItemLocations(currentLocationItem.id);
        
        // Reload inventory and warehouses data
        await loadInventoryItems();
        await loadWarehouses();
        
    } catch (error) {
        console.error('Error updating quantity:', error);
        showAlert(error.message, 'danger');
    }
}

async function removeFromLocation(warehouseId, itemId, warehouseName) {
    if (!confirm(`Are you sure you want to remove this item from ${warehouseName}?`)) {
        return;
    }
    
    try {
        const response = await fetch(
            `${API_BASE_URL}/warehouse-inventory/warehouse/${warehouseId}/item/${itemId}`,
            { method: 'DELETE' }
        );
        
        if (!response.ok) {
            const error = await response.text();
            throw new Error(error);
        }
        
        showAlert('Item removed from warehouse successfully', 'success');
        
        // Reload the locations in the modal
        await manageItemLocations(itemId);
        
        // Reload inventory and warehouses data
        await loadInventoryItems();
        await loadWarehouses();
        
    } catch (error) {
        console.error('Error removing item from warehouse:', error);
        showAlert(error.message, 'danger');
    }
}

async function transferFromModal() {
    const itemId = parseInt(document.getElementById('modalTransferItemId').value);
    const sourceWarehouseId = parseInt(document.getElementById('modalTransferSource').value);
    const destinationWarehouseId = parseInt(document.getElementById('modalTransferDestination').value);
    const quantity = parseInt(document.getElementById('modalTransferQuantity').value);
    
    if (!sourceWarehouseId || !destinationWarehouseId) {
        showAlert('Please select both source and destination warehouses', 'warning');
        return;
    }
    
    if (sourceWarehouseId === destinationWarehouseId) {
        showAlert('Source and destination warehouses must be different', 'warning');
        return;
    }
    
    const transferRequest = {
        itemId: itemId,
        sourceWarehouseId: sourceWarehouseId,
        destinationWarehouseId: destinationWarehouseId,
        quantity: quantity
    };
    
    try {
        const response = await fetch(`${API_BASE_URL}/warehouse-inventory/transfer`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(transferRequest)
        });
        
        if (!response.ok) {
            const error = await response.text();
            throw new Error(error);
        }
        
        showAlert('Item transferred successfully', 'success');
        
        // Reset form
        document.getElementById('modalTransferForm').reset();
        
        // Reload the locations in the modal
        await manageItemLocations(itemId);
        
        // Reload inventory and warehouses data
        await loadInventoryItems();
        await loadWarehouses();
        
    } catch (error) {
        console.error('Error transferring item:', error);
        showAlert(error.message, 'danger');
    }
}

// create a new Inventory Item in the database
async function saveInventoryItem() {
    const id = document.getElementById('inventoryId').value;
    const item = {
        serialNumber: document.getElementById('inventorySerial').value,
        productTypeId: parseInt(document.getElementById('inventoryProductType').value)
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

function editInventoryItem(id) {
    const item = allInventoryItems.find(i => i.id === id);
    if (!item) return;
    
    document.getElementById('inventoryId').value = item.id;
    document.getElementById('inventorySerial').value = item.serialNumber;
    document.getElementById('inventoryProductType').value = item.productType?.id || '';
    
    document.getElementById('inventoryForm').scrollIntoView({ behavior: 'smooth' });
}

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

// filter for inventory items based on the criteria passed
function filterInventoryItems() {
    const searchTerm = document.getElementById('inventorySearch')?.value.toLowerCase() || '';
    const warehouseFilter = document.getElementById('warehouseFilter')?.value || '';
    const productTypeFilter = document.getElementById('productTypeFilter')?.value || '';
    
    // Start with all items
    let filtered = [...allInventoryItems];
    
    // Apply search term (searches serial number and product name)
    if (searchTerm) {
        filtered = filtered.filter(item => {
            const serialMatch = item.serialNumber?.toLowerCase().includes(searchTerm);
            const productMatch = item.productType?.name?.toLowerCase().includes(searchTerm);
            return serialMatch || productMatch;
        });
    }
    
    // Apply warehouse filter
    if (warehouseFilter) {
        const warehouseId = parseInt(warehouseFilter);
        filtered = filtered.filter(item => {
            // Check if item exists in the selected warehouse
            return item.warehouseLocations?.some(loc => loc.warehouse?.id === warehouseId);
        });
    }
    
    // Apply product type filter
    if (productTypeFilter) {
        const productTypeId = parseInt(productTypeFilter);
        filtered = filtered.filter(item => {
            return item.productType?.id === productTypeId;
        });
    }
    
    // Update display with filtered items
    inventoryItems = filtered;
    displayInventoryItems();
}

function populateFilters() {
    // Populate warehouse filter
    const warehouseFilter = document.getElementById('warehouseFilter');
    if (warehouseFilter) {
        warehouseFilter.innerHTML = '<option value="">All Warehouses</option>';
        warehouses.forEach(warehouse => {
            const option = document.createElement('option');
            option.value = warehouse.id;
            option.textContent = warehouse.name;
            warehouseFilter.appendChild(option);
        });
    }
    
    // Populate product type filter
    const productTypeFilter = document.getElementById('productTypeFilter');
    if (productTypeFilter) {
        productTypeFilter.innerHTML = '<option value="">All Product Types</option>';
        productTypes.forEach(productType => {
            const option = document.createElement('option');
            option.value = productType.id;
            option.textContent = `${productType.name} (${productType.category})`;
            productTypeFilter.appendChild(option);
        });
    }
}

function updateFilterStats(count) {
    const statsElement = document.getElementById('filterStats');
    if (statsElement) {
        statsElement.textContent = `Showing ${count} of ${inventoryItems.length} items`;
    }
}

// show the transfer form 
function showTransferForm(itemId) {
    const item = inventoryItems.find(i => i.id === itemId);
    if (!item) return;
    
    // Set transfer form values
    document.getElementById('transferItemId').value = item.id;
    document.getElementById('transferItemName').textContent = `${item.productType.name} (${item.serialNumber})`;
    document.getElementById('transferCurrentWarehouse').textContent = item.warehouse.name;
    document.getElementById('transferCurrentQuantity').textContent = item.quantity;
    document.getElementById('transferSourceWarehouse').value = item.warehouse.id;
    document.getElementById('transferQuantity').max = item.quantity;
    document.getElementById('transferQuantity').value = item.quantity;
    
    // Populate destination warehouse dropdown (exclude current warehouse)
    const destSelect = document.getElementById('transferDestinationWarehouse');
    destSelect.innerHTML = '<option value="">Select destination warehouse...</option>';
    
    warehouses.forEach(warehouse => {
        if (warehouse.id !== item.warehouse.id && warehouse.status === 'ACTIVE') {
            const option = document.createElement('option');
            option.value = warehouse.id;
            option.textContent = `${warehouse.name} (Available: ${warehouse.availableCapacity})`;
            destSelect.appendChild(option);
        }
    });
    
    // Show transfer section and scroll to it
    document.getElementById('transferSection').style.display = 'block';
    document.getElementById('transferSection').scrollIntoView({ behavior: 'smooth' });
}

function cancelTransfer() {
    document.getElementById('transferForm').reset();
    document.getElementById('transferSection').style.display = 'none';
    document.getElementById('transferItemId').value = '';
}

async function transferInventoryItem() {
    const itemId = parseInt(document.getElementById('transferItemId').value);
    const sourceWarehouseId = parseInt(document.getElementById('transferSourceWarehouse').value);
    const destinationWarehouseId = parseInt(document.getElementById('transferDestinationWarehouse').value);
    const quantity = parseInt(document.getElementById('transferQuantity').value);
    
    if (!destinationWarehouseId) {
        showAlert('Please select a destination warehouse', 'warning');
        return;
    }
    
    const transferRequest = {
        itemId: itemId,
        sourceWarehouseId: sourceWarehouseId,
        destinationWarehouseId: destinationWarehouseId,
        quantity: quantity
    };
    
    try {
        const response = await fetch(`${API_BASE_URL}/inventory/transfer`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(transferRequest)
        });
        
        if (!response.ok) {
            const error = await response.text();
            throw new Error(error);
        }
        
        // Clear form and hide section
        cancelTransfer();
        
        // Reload data
        await loadInventoryItems();
        await loadWarehouses();
        
        showAlert('Item transferred successfully', 'success');
    } catch (error) {
        console.error('Error transferring item:', error);
        showAlert(error.message, 'danger');
    }
}

// Product Type 
async function loadProductTypes() {
    try {
        const response = await fetch(`${API_BASE_URL}/products`);
        productTypes = await response.json();
        
        if (currentSection === 'products') {
            displayProductTypes();
        }
        
        populateProductTypeSelects();
        return productTypes;
    } catch (error) {
        console.error('Error loading product types:', error);
        throw error;
    }
}

function displayProductTypes() {
    const grid = document.getElementById('productsGrid');
    
    if (productTypes.length === 0) {
        grid.innerHTML = '<p class="text-grey">No product types found</p>';
        return;
    }
    
    let html = '';
    productTypes.forEach(product => {
        html += `
            <div class="product-card">
                <div class="d-flex justify-content-between align-items-start mb-3">
                    <h5 class="text-yellow mb-0">${product.name}</h5>
                    <span class="badge badge-blue">${product.category}</span>
                </div>
                <p class="text-grey mb-3">${product.description || 'No description available'}</p>
                <p class="mb-3"><small class="text-grey"><i class="bi bi-box"></i> Unit: ${product.unitOfMeasure}</small></p>
                <div class="d-flex gap-2">
                    <button class="btn btn-bat-action btn-sm flex-fill" onclick="editProductType(${product.id})">
                        <i class="bi bi-pencil"></i> Edit
                    </button>
                    <button class="btn btn-bat-danger btn-sm flex-fill" onclick="deleteProductType(${product.id})">
                        <i class="bi bi-trash"></i> Delete
                    </button>
                </div>
            </div>
        `;
    });
    
    grid.innerHTML = html;
}

async function saveProductType() {
    const id = document.getElementById('productId').value;
    const product = {
        name: document.getElementById('productName').value,
        category: document.getElementById('productCategory').value,
        unitOfMeasure: document.getElementById('productUnit').value,
        description: document.getElementById('productDescription').value
    };
    
    try {
        const url = id ? `${API_BASE_URL}/products/${id}` : `${API_BASE_URL}/products`;
        const method = id ? 'PUT' : 'POST';
        
        const response = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(product)
        });
        
        if (!response.ok) throw new Error('Failed to save product type');
        
        document.getElementById('productForm').reset();
        document.getElementById('productId').value = '';
        
        await loadProductTypes();
        showAlert(`Product type ${id ? 'updated' : 'created'} successfully`, 'success');
    } catch (error) {
        console.error('Error saving product type:', error);
        showAlert('Failed to save product type', 'danger');
    }
}

function editProductType(id) {
    const product = productTypes.find(p => p.id === id);
    if (!product) return;
    
    document.getElementById('productId').value = product.id;
    document.getElementById('productName').value = product.name;
    document.getElementById('productCategory').value = product.category;
    document.getElementById('productUnit').value = product.unitOfMeasure;
    document.getElementById('productDescription').value = product.description || '';
    
    document.getElementById('productForm').scrollIntoView({ behavior: 'smooth' });
}

async function deleteProductType(id) {
    if (!confirm('Are you sure you want to delete this product type?')) return;
    
    try {
        const response = await fetch(`${API_BASE_URL}/products/${id}`, {
            method: 'DELETE'
        });
        
        if (!response.ok) throw new Error('Failed to delete product type');
        
        await loadProductTypes();
        showAlert('Product type deleted successfully', 'success');
    } catch (error) {
        console.error('Error deleting product type:', error);
        showAlert(error.message, 'danger');
    }
}

// helper methods to populate our elements when selected 
function populateWarehouseSelects() {
    const select = document.getElementById('inventoryWarehouse');
    if (!select) return;
    
    const currentValue = select.value;
    select.innerHTML = '<option value="">Select warehouse...</option>';
    
    warehouses.forEach(warehouse => {
        const option = document.createElement('option');
        option.value = warehouse.id;
        option.textContent = `${warehouse.name} (Available: ${warehouse.availableCapacity || 0})`;
        select.appendChild(option);
    });
    
    if (currentValue) select.value = currentValue;
}

function populateProductTypeSelects() {
    const select = document.getElementById('inventoryProductType');
    if (!select) return;
    
    const currentValue = select.value;
    select.innerHTML = '<option value="">Select product type...</option>';
    
    productTypes.forEach(product => {
        const option = document.createElement('option');
        option.value = product.id;
        option.textContent = `${product.name} (${product.category})`;
        select.appendChild(option);
    });
    
    if (currentValue) select.value = currentValue;
}

function populateFilterSelects() {
    // Populate warehouse filter
    const warehouseFilter = document.getElementById('filterWarehouse');
    if (warehouseFilter) {
        const currentValue = warehouseFilter.value;
        warehouseFilter.innerHTML = '<option value="">All Warehouses</option>';
        
        warehouses.forEach(warehouse => {
            const option = document.createElement('option');
            option.value = warehouse.id;
            option.textContent = warehouse.name;
            warehouseFilter.appendChild(option);
        });
        
        if (currentValue) warehouseFilter.value = currentValue;
    }
    // Populate product type filter
    const productFilter = document.getElementById('filterProduct');
    if (productFilter) {
        const currentValue = productFilter.value;
        productFilter.innerHTML = '<option value="">All Products</option>';
        
        productTypes.forEach(product => {
            const option = document.createElement('option');
            option.value = product.id;
            option.textContent = `${product.name} (${product.category})`;
            productFilter.appendChild(option);
        });
        
        if (currentValue) productFilter.value = currentValue;
    }
}