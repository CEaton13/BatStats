package com.skillstormproject1.batstats.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import com.skillstormproject1.batstats.models.ProductType;
import com.skillstormproject1.batstats.services.ProductTypeService;



@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://127.0.0.1:5500")
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

    @PostMapping
    public ResponseEntity<ProductType> createProductType(@RequestBody ProductType productType) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productTypeService.createProductType(productType));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductType> updateProductType(@PathVariable int id, @RequestBody ProductType productType) {
        return ResponseEntity.ok(productTypeService.updateProductType(id, productType));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductType(@PathVariable int id) {
        productTypeService.deleteProductType(id);
        return ResponseEntity.noContent().build();
    }

}
