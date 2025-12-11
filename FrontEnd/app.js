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
        
        //populateWarehouseSelects();
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