// API Base URL
const API_BASE_URL = 'http://localhost:8080/api';

let currentSection = 'dashboard';
let warehouses = [];
let inventoryItems = [];
let allInventoryItems = [];
let productTypes = [];
let locationModalInstance = null;
let currentManagedItem = null;
let activeTransferLocationId = null; 

// Initialize application
document.addEventListener('DOMContentLoaded', function() {
    console.log('BatStats Inventory System initialized');
    setupNavigation();
    loadDashboard();
    setupForms();
    setupSearchAndFilter();
    initializeLocationModal();
});

// Initialize location modal
function initializeLocationModal() {
    const modalElement = document.getElementById('locationModal');
    if (modalElement) {
        locationModalInstance = new bootstrap.Modal(modalElement);
        
        // Clean up when modal is hidden
        modalElement.addEventListener('hidden.bs.modal', function () {
            currentManagedItem = null;
            activeTransferLocationId = null;
            document.getElementById('addLocationForm').reset();
            document.getElementById('currentLocationsList').innerHTML = '';
            hideTransferForm();
        });
        
        // Setup add location form
        document.getElementById('addLocationForm').addEventListener('submit', async function(e) {
            e.preventDefault();
            await addItemToNewWarehouse();
        });
        
        // Setup transfer form
        document.getElementById('transferLocationForm').addEventListener('submit', async function(e) {
            e.preventDefault();
            await transferBetweenWarehouses();
        });
    }
}

// Navigation setup
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

// Forms setup
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
}

// Alerts
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

// Search and filter setup
function setupSearchAndFilter() {
    const searchInput = document.getElementById('inventorySearch');
    if (searchInput) {
        // Debounce search to avoid too many API calls
        let searchTimeout;
        searchInput.addEventListener('input', function(e) {
            clearTimeout(searchTimeout);
            searchTimeout = setTimeout(() => {
                performSearch();
            }, 300); // Wait 300ms after user stops typing
        });
    }
    
    const warehouseFilter = document.getElementById('warehouseFilter');
    if (warehouseFilter) {
        warehouseFilter.addEventListener('change', function(e) {
            filterInventoryItems();
        });
    }
    
    const productTypeFilter = document.getElementById('productTypeFilter');
    if (productTypeFilter) {
        productTypeFilter.addEventListener('change', function(e) {
            filterInventoryItems();
        });
    }
}

async function performSearch() {
    const searchTerm = document.getElementById('inventorySearch')?.value.trim() || '';
    
    try {
        if (searchTerm.length === 0) {
            // No search term - load all items
            await loadInventoryItems();
        } else if (searchTerm.length >= 2) {
            // Search requires at least 2 characters
            const response = await fetch(`${API_BASE_URL}/inventory/search?term=${encodeURIComponent(searchTerm)}`);
            if (!response.ok) {
                throw new Error('Search failed');
            }
            allInventoryItems = await response.json();
            inventoryItems = [...allInventoryItems];
            
            // Apply warehouse/product type filters if active
            filterInventoryItems();
        }
    } catch (error) {
        console.error('Error searching inventory:', error);
        showAlert('Search failed. Showing all items.', 'warning');
        await loadInventoryItems();
    }
}


// ==================== DASHBOARD ====================

//*************************************************************************************************************** */
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

// ==================== WAREHOUSES ====================
/***************************************************************************************************************** */
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
        
        document.getElementById('warehouseForm').reset();
        document.getElementById('warehouseId').value = '';
        
        await loadWarehouses();
        showAlert(`Warehouse ${id ? 'updated' : 'created'} successfully`, 'success');
    } catch (error) {
        console.error('Error saving warehouse:', error);
        showAlert('Failed to save warehouse', 'danger');
    }
}

function editWarehouse(id) {
    const warehouse = warehouses.find(w => w.id === id);
    if (!warehouse) return;
    
    document.getElementById('warehouseId').value = warehouse.id;
    document.getElementById('warehouseName').value = warehouse.name;
    document.getElementById('warehouseLocation').value = warehouse.location;
    document.getElementById('warehouseCapacity').value = warehouse.maxCapacity;
    document.getElementById('warehouseStatus').value = warehouse.status;
    
    document.getElementById('warehouseForm').scrollIntoView({ behavior: 'smooth' });
}

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

// ==================== INVENTORY ITEMS ====================

async function loadInventoryItems() {
    try {
           const response = await fetch(`${API_BASE_URL}/inventory`, {
            headers: {
                'Cache-Control': 'no-cache',
                'Pragma': 'no-cache'
            }
        });
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        allInventoryItems = await response.json();
        inventoryItems = [...allInventoryItems];

        console.log('Loaded inventory items:', inventoryItems);
        
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
        const serialNumber = item.serialNumber || 'N/A';
        const productName = item.productType?.name || 'Unknown Product';
        const locations = item.warehouseLocations || [];
        
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
        
        const totalQuantity = locations.reduce((sum, loc) => sum + (loc.quantity || 0), 0);
        
        html += `
            <tr>
                <td><code class="text-yellow">${serialNumber}</code></td>
                <td>${productName}</td>
                <td>${locationsHtml}</td>
                <td><span class="badge badge-yellow">${totalQuantity}</span></td>
                <td>${locations.length} location${locations.length !== 1 ? 's' : ''}</td>
                <td>
                    <button class="btn btn-bat-action btn-sm" onclick="manageItemLocations(${item.id})" title="Manage warehouse locations and quantities">
                        <i class="bi bi-pin-map"></i> Manage
                    </button>
                    <button class="btn btn-bat-danger btn-sm" onclick="deleteInventoryItem(${item.id})" title="Delete this item completely">
                        <i class="bi bi-trash"></i> Delete
                    </button>
                </td>
            </tr>
        `;
    });
    
    tbody.innerHTML = html;
}

async function saveInventoryItem() {
    const id = document.getElementById('inventoryId').value;
    const item = {
        serialNumber: document.getElementById('inventorySerial').value,
        productTypeId: parseInt(document.getElementById('inventoryProductType').value),
        initialWarehouseId: parseInt(document.getElementById('inventoryWarehouse').value),
        initialQuantity: parseInt(document.getElementById('inventoryQuantity').value)
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

function filterInventoryItems() {
    const warehouseFilter = document.getElementById('warehouseFilter')?.value || '';
    const productTypeFilter = document.getElementById('productTypeFilter')?.value || '';
    
    // Start with all items (which may already be filtered by search)
    let filtered = [...allInventoryItems];
    
    // Apply warehouse filter
    if (warehouseFilter) {
        const warehouseId = parseInt(warehouseFilter);
        filtered = filtered.filter(item => {
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
    
    inventoryItems = filtered;
    displayInventoryItems();
}

function populateFilters() {
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

// ==================== LOCATION MANAGEMENT MODAL ====================

async function manageItemLocations(itemId) {
    const item = allInventoryItems.find(i => i.id === itemId);
    if (!item) {
        showAlert('Item not found', 'danger');
        return;
    }
    
    currentManagedItem = item;
    
    // Update modal header info
    document.getElementById('modalItemName').textContent = item.productType?.name || 'Unknown';
    document.getElementById('modalItemSerial').textContent = item.serialNumber || 'N/A';
    document.getElementById('modalTotalQuantity').textContent = item.totalQuantity || 0;
    document.getElementById('addLocationItemId').value = item.id;
    
    // Display current locations
    displayCurrentLocations(item);
    
    // Populate available warehouses for adding
    populateAvailableWarehouses(item);
    
    // Hide transfer form initially
    hideTransferForm();
    
    // Show the modal
    if (locationModalInstance) {
        locationModalInstance.show();
    }
}

function displayCurrentLocations(item) {
    const container = document.getElementById('currentLocationsList');
    const locations = item.warehouseLocations || [];
    
    if (locations.length === 0) {
        container.innerHTML = '<p class="text-grey">This item is not currently stored in any warehouse.</p>';
        return;
    }
    
    let html = '';
    locations.forEach(location => {
        const warehouse = location.warehouse;
        const warehouseId = warehouse?.id || 0;
        const warehouseName = warehouse?.name || 'Unknown';
        const quantity = location.quantity || 0;
        const locationId = location.id;
        
        html += `
            <div class="card bg-secondary mb-3">
                <div class="card-body">
                    <div class="row align-items-center">
                        <div class="col-md-3">
                            <h6 class="text-yellow mb-1">${warehouseName}</h6>
                            <small class="text-grey">${warehouse?.location || 'Unknown location'}</small>
                        </div>
                        <div class="col-md-3">
                            <label class="form-label small">Quantity</label>
                            <input type="number" class="form-control form-control-sm" 
                                   id="qty-${locationId}" value="${quantity}" min="1">
                        </div>
                        <div class="col-md-6 text-end">
                            <button class="btn btn-bat-action btn-sm" onclick="updateLocationQuantity(${locationId})">
                                <i class="bi bi-save"></i> Update
                            </button>
                            <button class="btn btn-warning btn-sm" onclick="showTransferFormInModal(${locationId}, '${warehouseName}', ${quantity})">
                                <i class="bi bi-arrow-left-right"></i> Transfer
                            </button>
                            <button class="btn btn-bat-danger btn-sm" onclick="removeFromLocation(${warehouseId}, ${item.id})">
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

function populateAvailableWarehouses(item) {
    const select = document.getElementById('addLocationWarehouse');
    select.innerHTML = '<option value="">Select warehouse...</option>';
    
    const existingWarehouseIds = (item.warehouseLocations || [])
        .map(loc => loc.warehouse?.id)
        .filter(id => id != null);
    
    warehouses.forEach(warehouse => {
        if (warehouse.status === 'ACTIVE' && !existingWarehouseIds.includes(warehouse.id)) {
            const option = document.createElement('option');
            option.value = warehouse.id;
            option.textContent = `${warehouse.name} (Available: ${warehouse.availableCapacity || 0})`;
            select.appendChild(option);
        }
    });
}

async function updateLocationQuantity(locationId) {
    const quantityInput = document.getElementById(`qty-${locationId}`);
    const newQuantity = parseInt(quantityInput.value);
    
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
        
        await Promise.all([loadInventoryItems(), loadWarehouses()]);
        
        if (currentManagedItem) {
            const updatedItem = allInventoryItems.find(i => i.id === currentManagedItem.id);
            if (updatedItem) {
                currentManagedItem = updatedItem;
                displayCurrentLocations(updatedItem);
                document.getElementById('modalTotalQuantity').textContent = updatedItem.totalQuantity || 0;
            }
        }
    } catch (error) {
        console.error('Error updating quantity:', error);
        showAlert(error.message || 'Failed to update quantity', 'danger');
    }
}

async function removeFromLocation(warehouseId, itemId) {
    console.log('removeFromLocation called with:', { warehouseId, itemId });
    
    // Validate parameters
    if (!warehouseId || !itemId) {
        console.error('Invalid parameters:', { warehouseId, itemId });
        showAlert('Invalid warehouse or item ID', 'danger');
        return;
    }
    
    if (!confirm('Are you sure you want to remove this item from this warehouse?')) {
        console.log('User cancelled removal');
        return;
    }
    
    try {
        const url = `${API_BASE_URL}/warehouse-inventory/warehouse/${warehouseId}/item/${itemId}`;
        console.log('DELETE request to:', url);
        
        const response = await fetch(url, {
                        method: 'DELETE',
            headers: {
                'Cache-Control': 'no-cache',
                'Pragma': 'no-cache'
            }
        });
        
        console.log('Response status:', response.status);
        
        if (!response.ok) {
            const errorText = await response.text();
            console.error('Server error:', errorText);
            throw new Error(errorText || 'Failed to remove item');
        }
        
        showAlert('Item removed from warehouse successfully', 'success');
        
  // Optimistically update the UI immediately
        if (currentManagedItem) {
            // Remove the location from the current item's warehouseLocations array
            currentManagedItem.warehouseLocations = currentManagedItem.warehouseLocations.filter(
                loc => loc.warehouse.id !== warehouseId
            );
            
            console.log('Locations after removal:', currentManagedItem.warehouseLocations.length);
            
            // Check if item still has locations
            if (currentManagedItem.warehouseLocations.length > 0) {
                // Item still has locations, update the display immediately
                displayCurrentLocations(currentManagedItem);
                populateAvailableWarehouses(currentManagedItem);
                
                // Recalculate total quantity
                const newTotal = currentManagedItem.warehouseLocations.reduce(
                    (sum, loc) => sum + (loc.quantity || 0), 0
                );
                document.getElementById('modalTotalQuantity').textContent = newTotal;
            } else {
                // Item has no more locations, close modal
                console.log('Item no longer has any locations, closing modal');
                if (locationModalInstance) {
                    locationModalInstance.hide();
                }
            }
        }
        
        // Reload data in the background with a small delay to ensure DB commit
        setTimeout(async () => {
            await Promise.all([loadInventoryItems(), loadWarehouses()]);
            
            // Update currentManagedItem with fresh data if modal is still open
            if (currentManagedItem && locationModalInstance._isShown) {
                const updatedItem = allInventoryItems.find(i => i.id === currentManagedItem.id);
                if (updatedItem) {
                    console.log('Refreshed item data:', updatedItem);
                    currentManagedItem = updatedItem;
                    if (updatedItem.warehouseLocations && updatedItem.warehouseLocations.length > 0) {
                        displayCurrentLocations(updatedItem);
                        populateAvailableWarehouses(updatedItem);
                        document.getElementById('modalTotalQuantity').textContent = updatedItem.totalQuantity || 0;
                    }
                }
            }
        }, 500); // 500ms delay to ensure database commit
        
    } catch (error) {
        console.error('Error removing item from warehouse:', error);
        showAlert(error.message || 'Failed to remove item', 'danger');
    }
}

async function addItemToNewWarehouse() {
    const itemId = parseInt(document.getElementById('addLocationItemId').value);
    const warehouseId = parseInt(document.getElementById('addLocationWarehouse').value);
    const quantity = parseInt(document.getElementById('addLocationQuantity').value);
    
    if (!warehouseId) {
        showAlert('Please select a warehouse', 'warning');
        return;
    }
    
    if (!quantity || quantity < 1) {
        showAlert('Please enter a valid quantity', 'warning');
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
        
        document.getElementById('addLocationForm').reset();
        document.getElementById('addLocationItemId').value = itemId;
        
        await Promise.all([loadInventoryItems(), loadWarehouses()]);
        
        if (currentManagedItem) {
            const updatedItem = allInventoryItems.find(i => i.id === currentManagedItem.id);
            if (updatedItem) {
                currentManagedItem = updatedItem;
                displayCurrentLocations(updatedItem);
                populateAvailableWarehouses(updatedItem);
                document.getElementById('modalTotalQuantity').textContent = updatedItem.totalQuantity || 0;
            }
        }
    } catch (error) {
        console.error('Error adding item to warehouse:', error);
        showAlert(error.message || 'Failed to add item to warehouse', 'danger');
    }
}

// ==================== TRANSFER FUNCTIONALITY IN MODAL ====================

function showTransferFormInModal(locationId, sourceWarehouseName, currentQuantity) {
    activeTransferLocationId = locationId;
    
    // Populate transfer form
    document.getElementById('transferSourceInfo').textContent = sourceWarehouseName;
    document.getElementById('transferAvailableQty').textContent = currentQuantity;
    document.getElementById('transferQtyInput').max = currentQuantity;
    document.getElementById('transferQtyInput').value = currentQuantity;
    
    // Populate destination warehouse dropdown (exclude source warehouse)
    const sourceLocation = currentManagedItem.warehouseLocations.find(loc => loc.id === locationId);
    const sourceWarehouseId = sourceLocation?.warehouse?.id;
    
    const destSelect = document.getElementById('transferDestWarehouse');
    destSelect.innerHTML = '<option value="">Select destination...</option>';
    
    warehouses.forEach(warehouse => {
        if (warehouse.id !== sourceWarehouseId && warehouse.status === 'ACTIVE') {
            const option = document.createElement('option');
            option.value = warehouse.id;
            option.textContent = `${warehouse.name} (Available: ${warehouse.availableCapacity || 0})`;
            destSelect.appendChild(option);
        }
    });
    
    // Show transfer section, hide add section
    document.getElementById('transferLocationSection').style.display = 'block';
    document.getElementById('addLocationSection').style.display = 'none';
    
    // Scroll to transfer form
    document.getElementById('transferLocationSection').scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

function hideTransferForm() {
    activeTransferLocationId = null;
    document.getElementById('transferLocationSection').style.display = 'none';
    document.getElementById('addLocationSection').style.display = 'block';
    document.getElementById('transferLocationForm').reset();
}

async function transferBetweenWarehouses() {
    if (!activeTransferLocationId) {
        showAlert('No active transfer', 'danger');
        return;
    }
    
    const destinationWarehouseId = parseInt(document.getElementById('transferDestWarehouse').value);
    const quantity = parseInt(document.getElementById('transferQtyInput').value);
    
    if (!destinationWarehouseId) {
        showAlert('Please select a destination warehouse', 'warning');
        return;
    }
    
    if (!quantity || quantity < 1) {
        showAlert('Please enter a valid quantity', 'warning');
        return;
    }
    
    // Find source warehouse from the active location
    const sourceLocation = currentManagedItem.warehouseLocations.find(
        loc => loc.id === activeTransferLocationId
    );
    
    if (!sourceLocation) {
        showAlert('Source location not found', 'danger');
        return;
    }
    
    const sourceWarehouseId = sourceLocation.warehouse.id;
    
    const transferRequest = {
        itemId: currentManagedItem.id,
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
        
        showAlert('Items transferred successfully', 'success');
        
        // Hide transfer form
        hideTransferForm();
        
        // Reload data
        await Promise.all([loadInventoryItems(), loadWarehouses()]);
        
        if (currentManagedItem) {
            const updatedItem = allInventoryItems.find(i => i.id === currentManagedItem.id);
            if (updatedItem) {
                currentManagedItem = updatedItem;
                displayCurrentLocations(updatedItem);
                populateAvailableWarehouses(updatedItem);
                document.getElementById('modalTotalQuantity').textContent = updatedItem.totalQuantity || 0;
            }
        }
    } catch (error) {
        console.error('Error transferring items:', error);
        showAlert(error.message || 'Failed to transfer items', 'danger');
    }
}

// ==================== PRODUCT TYPES ====================

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

// ==================== HELPER FUNCTIONS ====================

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
