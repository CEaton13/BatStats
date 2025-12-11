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
    setupForms();
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