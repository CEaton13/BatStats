package com.skillstormproject1.batstats.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.skillstormproject1.batstats.models.InventoryItem;
import com.skillstormproject1.batstats.models.ProductType;
import com.skillstormproject1.batstats.services.InventoryItemService;
import com.skillstormproject1.batstats.services.ProductTypeService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
public class ProductTypeController {

    private final ProductTypeService productTypeService;

    public ProductTypeController(ProductTypeService productTypeService) {
        this.productTypeService = productTypeService;
    }

    //mapping out all the services for the api controller

    // retreive and list all product types
    @GetMapping
    public ResponseEntity<List<ProductType>> getAllProductTypes() {
        return ResponseEntity.ok(productTypeService.getAllProductTypes());
    }
    
    // find a product type of a specific id
    @GetMapping("/{id}")
    public ResponseEntity<ProductType> getProductTypeById(@PathVariable int id) {
        return ResponseEntity.ok(productTypeService.getProductTypeById(id));
    }

    // get all the product types of a specific category
    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductType>> getProductTypeByCategory(@PathVariable String category) {
        return ResponseEntity.ok(productTypeService.getProductTypeByCategory(category));
    }

    /**
     * createProductType PostMapping
     * updateProdcutType PutMapping
     * deleteProductType DeleteMapping 
     */


}
