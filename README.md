[README.md](https://github.com/user-attachments/files/24442035/README.md)
# 🦇 BatStats - Inventory Management System

> *"It's not who I am underneath, but what I do that defines me."* - Batman

A comprehensive, Batman-themed full-stack inventory management system designed to empower warehouse administrators with complete control over inventory across multiple locations.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.8-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-blue)
![Bootstrap](https://img.shields.io/badge/Bootstrap-5.3-purple)

## 📋 Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Prerequisites](#-prerequisites)
- [Installation](#-installation)
- [Project Structure](#-project-structure)
- [API Documentation](#-api-documentation)
- [Database Schema](#-database-schema)
- [Usage Guide](#-usage-guide)
- [Development Principles](#-development-principles)
- [Future Enhancements](#-future-enhancements)
- [Contributing](#-contributing)
- [License](#-license)

## ✨ Features

### Core Functionality

#### 🏢 Warehouse Management
- **CRUD Operations**: Create, read, update, and delete warehouses
- **Capacity Tracking**: Real-time monitoring of warehouse utilization
- **Status Management**: Active/Inactive warehouse status control
- **Capacity Alerts**: Visual warnings when warehouses approach capacity limits (75%, 90%)

#### 📦 Inventory Management
- **Item Tracking**: Comprehensive inventory item management with auto-generated serial numbers
- **Multi-Location Support**: Track items across multiple warehouse locations
- **Product Association**: Link items to product types with categories
- **Search & Filter**: Advanced search by serial number, product name, warehouse, or category
- **Quantity Management**: Update quantities at specific warehouse locations

#### 🔄 Transfer Operations
- **Inter-Warehouse Transfers**: Move items between warehouses with full or partial quantities
- **Capacity Validation**: Automatic checks to prevent overstocking
- **In-Modal Transfers**: Seamless transfer interface within location management modal

#### 🎨 Product Type Management
- **Category Organization**: Organize products by customizable categories
- **Unit of Measure**: Track items by appropriate units (packs, cases, boxes)
- **Auto Serial Generation**: Intelligent serial number generation based on product categories

#### 📊 Dashboard & Analytics
- **Real-Time Statistics**: Live counts of warehouses, items, and product types
- **Capacity Overview**: Visual progress bars for warehouse utilization
- **Alert System**: Prominent alerts for warehouses nearing capacity
- **Average Capacity**: System-wide capacity utilization tracking

### Edge Case Handling

- ✅ **Capacity Overflow Prevention**: Validates capacity before adding/transferring items
- ✅ **Duplicate Serial Number Detection**: Prevents duplicate inventory entries
- ✅ **Cascade Deletion**: Properly handles relationships when deleting items
- ✅ **Transaction Management**: Ensures data consistency with @Transactional annotations

## 🛠 Tech Stack

### Backend
- **Java 17**: Modern Java with latest features
- **Spring Boot 3.5.8**: Enterprise-grade framework
  - Spring Data JPA: Database abstraction layer
  - Spring Web: RESTful API development
  - Spring AOP: Cross-cutting concerns (exception handling, logging)
  - Spring Validation: Input validation
- **PostgreSQL**: Robust relational database
  - Database triggers for automated capacity management
  - Normalized schema design
- **Maven**: Dependency management and build automation

### Frontend
- **HTML5/CSS3**: Modern semantic markup and styling
- **JavaScript (ES6+)**: Dynamic client-side functionality
- **Bootstrap 5.3**: Responsive UI framework
- **Bootstrap Icons**: Comprehensive icon library

### Development Practices
- Constructor-based dependency injection (no @Autowired)
- No Lombok dependency - explicit, readable code
- Integer primary keys for all entities
- RESTful API design principles
- Comprehensive exception handling with custom exceptions

## 📋 Prerequisites

Before you begin, ensure you have the following installed:

- **Java Development Kit (JDK) 17** or higher
  - [Download JDK 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
- **PostgreSQL 12+**
  - [Download PostgreSQL](https://www.postgresql.org/download/)
- **Maven 3.6+** (or use included Maven Wrapper)
- **Git** for version control
- A modern web browser (Chrome, Firefox, Edge)
- A code editor (VS Code, IntelliJ IDEA, Eclipse)

## 🚀 Installation

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/batstats.git
cd batstats
```

### 2. Database Setup

Create the PostgreSQL database:

```bash
# Connect to PostgreSQL
psql -U postgres

# Create database
CREATE DATABASE Batcave;

# Create user (optional, or use default postgres user)
CREATE USER your_username WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE Batcave TO your_username;
```

**Configure Database Connection:**

Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/Batcave
    username: postgres  # Change to your username
    password: skillstorm  # Change to your password
```

**Create Database Triggers:**

The application uses PostgreSQL triggers to automatically manage warehouse capacity. Run this SQL script after the application creates the tables:

```sql
-- Trigger to update warehouse capacity on INSERT
CREATE OR REPLACE FUNCTION update_capacity_on_insert()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE warehouses
    SET current_capacity = current_capacity + NEW.quantity
    WHERE id = NEW.warehouse_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_insert_warehouse_inventory
AFTER INSERT ON warehouse_inventory
FOR EACH ROW
EXECUTE FUNCTION update_capacity_on_insert();

-- Trigger to update warehouse capacity on UPDATE
CREATE OR REPLACE FUNCTION update_capacity_on_update()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE warehouses
    SET current_capacity = current_capacity - OLD.quantity + NEW.quantity
    WHERE id = NEW.warehouse_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_warehouse_inventory
AFTER UPDATE ON warehouse_inventory
FOR EACH ROW
EXECUTE FUNCTION update_capacity_on_update();

-- Trigger to update warehouse capacity on DELETE
CREATE OR REPLACE FUNCTION update_capacity_on_delete()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE warehouses
    SET current_capacity = current_capacity - OLD.quantity
    WHERE id = OLD.warehouse_id;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_delete_warehouse_inventory
AFTER DELETE ON warehouse_inventory
FOR EACH ROW
EXECUTE FUNCTION update_capacity_on_delete();
```

### 3. Backend Setup

```bash
# Navigate to project root
cd batstats

# Build the project (using Maven Wrapper)
./mvnw clean install

# Or if you have Maven installed globally
mvn clean install

# Run the application
./mvnw spring-boot:run
# OR
mvn spring-boot:run
```

The backend server will start on `http://localhost:8080`

**Verify Backend is Running:**
- Open browser to `http://localhost:8080/api/warehouses`
- Should return an empty JSON array `[]` if no data exists

### 4. Frontend Setup

The frontend uses vanilla JavaScript and can be served with any local server. Using VS Code's Live Server extension:

1. **Install Live Server Extension** (if using VS Code)
   - Open VS Code
   - Go to Extensions (Ctrl+Shift+X)
   - Search for "Live Server"
   - Install the extension by Ritwick Dey

2. **Start the Frontend**
   ```bash
   cd FrontEnd
   # Right-click on index.html and select "Open with Live Server"
   # Or use the "Go Live" button in VS Code status bar
   ```

The frontend will be available at `http://127.0.0.1:5500` (default Live Server port)

**Alternative: Using Python's HTTP Server**
```bash
cd FrontEnd
python -m http.server 5500
# Then open http://localhost:5500 in your browser
```

### 5. Verify Installation

1. Open browser to `http://127.0.0.1:5500`
2. You should see the BatStats dashboard
3. Try creating a warehouse, product type, and inventory item

## 📁 Project Structure

```
batstats/
├── src/
│   ├── main/
│   │   ├── java/com/skillstormproject1/batstats/
│   │   │   ├── aspect/              # AOP for exception handling
│   │   │   │   └── RestExceptionHandlerAspect.java
│   │   │   ├── controller/          # REST API controllers
│   │   │   │   ├── InventoryItemController.java
│   │   │   │   ├── ProductTypeController.java
│   │   │   │   ├── WarehouseController.java
│   │   │   │   └── WarehouseInventoryController.java
│   │   │   ├── dtos/                # Data Transfer Objects
│   │   │   │   ├── AddItemToWarehouseDTO.java
│   │   │   │   ├── InventoryItemDTO.java
│   │   │   │   ├── TransferRequestDTO.java
│   │   │   │   ├── WarehouseDTO.java
│   │   │   │   └── WarehouseLocationDTO.java
│   │   │   ├── exceptions/          # Custom exception classes
│   │   │   │   ├── DuplicateSerialNumberException.java
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   └── WarehouseCapacityExceededException.java
│   │   │   ├── models/              # JPA Entity classes
│   │   │   │   ├── InventoryItem.java
│   │   │   │   ├── ProductType.java
│   │   │   │   ├── Warehouse.java
│   │   │   │   └── WarehouseInventory.java
│   │   │   ├── repositories/        # Spring Data JPA repositories
│   │   │   │   ├── InventoryItemRepository.java
│   │   │   │   ├── ProductTypeRepository.java
│   │   │   │   ├── WarehouseInventoryRepository.java
│   │   │   │   └── WarehouseRepository.java
│   │   │   ├── services/            # Business logic layer
│   │   │   │   ├── InventoryItemService.java
│   │   │   │   ├── ProductTypeService.java
│   │   │   │   ├── WarehouseInventoryService.java
│   │   │   │   └── WarehouseService.java
│   │   │   └── BatstatsApplication.java
│   │   └── resources/
│   │       └── application.yml      # Application configuration
│   └── test/
│       └── java/                    # Test classes
├── FrontEnd/
│   ├── index.html                   # Main HTML file
│   ├── styles.css                   # Batman-themed styling
│   └── app.js                       # Frontend JavaScript logic
├── pom.xml                          # Maven dependencies
├── .gitignore
└── README.md
```

## 📡 API Documentation

### Base URL
```
http://localhost:8080/api
```

### Warehouses API

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/warehouses` | Get all warehouses |
| GET | `/warehouses/{id}` | Get warehouse by ID |
| GET | `/warehouses/status/{status}` | Get warehouses by status |
| GET | `/warehouses/near-capacity?threshold={percent}` | Get warehouses near capacity |
| POST | `/warehouses` | Create new warehouse |
| PUT | `/warehouses/{id}` | Update warehouse |
| DELETE | `/warehouses/{id}` | Delete warehouse |

**Example: Create Warehouse**
```json
POST /api/warehouses
{
  "name": "Batcave Main Storage",
  "location": "Gotham City Underground",
  "maxCapacity": 10000,
  "status": "ACTIVE"
}
```

### Inventory Items API

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/inventory` | Get all inventory items |
| GET | `/inventory/{id}` | Get item by ID |
| GET | `/inventory/search?term={searchTerm}` | Search items |
| GET | `/inventory/multi-location` | Get items in multiple warehouses |
| POST | `/inventory` | Create new item |
| PUT | `/inventory/{id}` | Update item |
| DELETE | `/inventory/{id}` | Delete item |

**Example: Create Inventory Item**
```json
POST /api/inventory
{
  "serialNumber": "",  // Leave empty for auto-generation
  "productTypeId": 1,
  "initialWarehouseId": 1,
  "initialQuantity": 50
}
```

### Warehouse Inventory API

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/warehouse-inventory/warehouse/{id}` | Get all items in warehouse |
| GET | `/warehouse-inventory/item/{id}` | Get all locations for item |
| GET | `/warehouse-inventory/item/{id}/total` | Get total quantity for item |
| POST | `/warehouse-inventory` | Add item to warehouse |
| PUT | `/warehouse-inventory/{id}` | Update quantity at location |
| DELETE | `/warehouse-inventory/warehouse/{warehouseId}/item/{itemId}` | Remove item from warehouse |
| POST | `/warehouse-inventory/transfer` | Transfer items between warehouses |

**Example: Transfer Items**
```json
POST /api/warehouse-inventory/transfer
{
  "itemId": 1,
  "sourceWarehouseId": 1,
  "destinationWarehouseId": 2,
  "quantity": 25
}
```

### Product Types API

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/products` | Get all product types |
| GET | `/products/{id}` | Get product type by ID |
| GET | `/products/category/{category}` | Get product types by category |
| POST | `/products` | Create new product type |
| PUT | `/products/{id}` | Update product type |
| DELETE | `/products/{id}` | Delete product type |

**Example: Create Product Type**
```json
POST /api/products
{
  "name": "Batarang",
  "category": "Weapons",
  "unitOfMeasure": "pack",
  "description": "Standard throwing weapons"
}
```

## 🗄 Database Schema

### Entity Relationships

```
ProductType (1) ──────< (Many) InventoryItem
                                    │
                                    │ (Many)
                                    │
                                    ▼
                          WarehouseInventory (Junction Table)
                                    │
                                    │ (Many)
                                    │
                                    ▼
Warehouse (1) ──────< (Many) WarehouseInventory
```

### Key Tables

**warehouses**
- `id` (PK): Integer
- `name`: String (100)
- `location`: String (200)
- `max_capacity`: Integer
- `current_capacity`: Integer
- `status`: String (20)
- `created_at`: Timestamp
- `updated_at`: Timestamp

**inventory_items**
- `id` (PK): Integer
- `serial_number` (Unique): String (50)
- `product_type_id` (FK): Integer
- `created_at`: Timestamp
- `updated_at`: Timestamp

**product_types**
- `id` (PK): Integer
- `name` (Unique): String (100)
- `category`: String (50)
- `unit_of_measure`: String
- `description`: Text
- `created_at`: Timestamp
- `updated_at`: Timestamp

**warehouse_inventory** (Junction Table)
- `id` (PK): Integer
- `warehouse_id` (FK): Integer
- `inventory_item_id` (FK): Integer
- `quantity`: Integer
- `created_at`: Timestamp
- `updated_at`: Timestamp

## 📖 Usage Guide

### Creating Your First Warehouse

1. Navigate to the **Warehouses** tab
2. Fill in the warehouse form:
   - **Name**: e.g., "Batcave Arsenal"
   - **Location**: e.g., "Level B3"
   - **Max Capacity**: e.g., 5000
   - **Status**: Select "Active"
3. Click **Save**

### Adding Product Types

1. Navigate to the **Products** tab
2. Fill in the product form:
   - **Product Name**: e.g., "Smoke Pellets"
   - **Category**: e.g., "Gadgets"
   - **Unit**: e.g., "box"
   - **Description**: Optional description
3. Click **Save**

### Managing Inventory Items

1. Navigate to the **Inventory** tab
2. Fill in the inventory form:
   - **Serial Number**: Leave empty for auto-generation
   - **Product Type**: Select from dropdown
   - **Warehouse**: Select destination warehouse
   - **Quantity**: Enter amount
3. Click **Save**

### Using Location Management

1. Click **Manage** on any inventory item
2. In the modal, you can:
   - **View Locations**: See all warehouses storing this item
   - **Update Quantity**: Modify quantity at any location
   - **Transfer Items**: Move items between warehouses
   - **Add to Warehouse**: Place items in additional warehouses
   - **Remove from Location**: Remove items from a warehouse

### Transferring Items

**Method 1: In Location Modal**
1. Click **Manage** on an item
2. Click **Transfer** next to a location
3. Select destination warehouse
4. Enter quantity to transfer
5. Click **Transfer**

**Method 2: Direct API Call**
Use the `/warehouse-inventory/transfer` endpoint with appropriate payload

### Searching and Filtering

- **Search**: Type in the search box (minimum 2 characters)
  - Searches serial numbers and product names
- **Filter by Warehouse**: Select a warehouse from dropdown
- **Filter by Product Type**: Select a product type from dropdown
- Filters can be combined for precise results

## 💡 Development Principles

This project follows industry best practices:

### Clean Architecture
- **Separation of Concerns**: Controller → Service → Repository layers
- **Single Responsibility**: Each class has one clear purpose
- **Dependency Inversion**: Depends on abstractions, not concretions

### Code Quality
- **No Lombok**: Explicit code for better readability and debugging
- **Constructor Injection**: Immutable dependencies, easier testing
- **Comprehensive Exception Handling**: Custom exceptions with AOP
- **DTOs for Data Transfer**: Separation between API and domain models
- **Input Validation**: Bean validation on all inputs

### Database Design
- **Normalized Schema**: Reduces redundancy, maintains integrity
- **Junction Tables**: Proper many-to-many relationships
- **Database Triggers**: Automated capacity management
- **Cascade Operations**: Proper handling of related entities

### Frontend Design
- **Responsive Design**: Mobile-friendly Bootstrap layout
- **Progressive Enhancement**: Works without JavaScript for basic features
- **Semantic HTML**: Accessible and SEO-friendly
- **CSS Custom Properties**: Easy theme customization
- **Flat Design Principles**: Clean, modern aesthetic

## 🚀 Future Enhancements

### Planned Features

#### Phase 1: User Management
- [ ] User authentication and authorization
- [ ] Role-based access control (Admin, Manager, Viewer)
- [ ] User activity logging

#### Phase 2: Advanced Analytics
- [ ] Capacity trends over time
- [ ] Inventory turnover rates
- [ ] Warehouse utilization reports
- [ ] Export to PDF/Excel

#### Phase 3: Automation
- [ ] Low stock alerts
- [ ] Automatic reordering suggestions
- [ ] Batch import/export operations
- [ ] Email notifications

#### Phase 4: Advanced Features
- [ ] Item expiration tracking
- [ ] QR code generation for items
- [ ] Mobile app integration
- [ ] Real-time updates with WebSockets
- [ ] Barcode scanning support

#### Phase 5: Batman Theme Extensions
- [ ] Mission tracking integration
- [ ] Threat level indicators
- [ ] Equipment maintenance schedules
- [ ] Batmobile inventory tracking

## 🤝 Contributing

Contributions are welcome! Please follow these guidelines:

1. **Fork the repository**
2. **Create a feature branch**
   ```bash
   git checkout -b feature/AmazingFeature
   ```
3. **Commit your changes**
   ```bash
   git commit -m 'Add some AmazingFeature'
   ```
4. **Push to the branch**
   ```bash
   git push origin feature/AmazingFeature
   ```
5. **Open a Pull Request**

### Coding Standards
- Follow existing code style
- Write meaningful commit messages
- Add comments for complex logic
- Update documentation as needed
- Add tests for new features

## 📝 License

This project is part of a portfolio/learning project. Feel free to use it for educational purposes.

## 👤 Author

**Charles Eaton**
- GitHub: [CEaton13](https://github.com/CEaton13)
  
## 🙏 Acknowledgments

- Inspired by Batman and the need for organized superhero equipment management
- Built as part of the SkillStorm training program
- Thanks to the Spring Boot and Bootstrap communities

## 📞 Support

If you encounter any issues or have questions:

1. Check the [API Documentation](#-api-documentation)
2. Review the [Installation Guide](#-installation)
3. Open an issue on GitHub
4. Contact the maintainer

---

<div align="center">

**Built with 🦇 by Batman fans, for Batman fans**

*"I wear a mask. And that mask, it's not to hide who I am, but to create what I am."*

</div>
