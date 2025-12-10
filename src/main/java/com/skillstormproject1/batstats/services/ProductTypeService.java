package com.skillstormproject1.batstats.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.skillstormproject1.batstats.models.ProductType;
import com.skillstormproject1.batstats.repositories.ProductTypeRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class ProductTypeService {

    private final ProductTypeRepository productTypeRepository;

    public ProductTypeService(ProductTypeRepository productTypeRepository) {
        this.productTypeRepository = productTypeRepository;
    }

    // finds all product types using built in findAll()
    public List<ProductType> getAllProductTypes(){
        return productTypeRepository.findAll();
    }
    
    // find a product by the id provided
    public ProductType getProductTypeById(int id){
        return productTypeRepository.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Product Type not found with id: " + id));    // need to throw an exception if it doesn't find that id
    }

    // find a product by category name
    public List<ProductType> getProductTypeByCategory(String category) {
        return productTypeRepository.findByCategory(category);
    }

    /**
     * createProductType - .save()
     * updateProductType - set all params
     * deleteProductType - exists boolean and exception
     */

    public ProductType createProductType(ProductType productType) {
        return productTypeRepository.save(productType);
    }

    public ProductType updateProductType(int id, ProductType productType) {
        ProductType existing = getProductTypeById(id);
        existing.setName(productType.getName());
        existing.setCategory(productType.getCategory());
        existing.setUnitOfMeasure(productType.getUnitOfMeasure());
        return productTypeRepository.save(existing);
    }
    
    public void deleteProductType(int id) {
        if (!productTypeRepository.existsById(id)) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,"Product Type not found with id: " + id);
            }
        productTypeRepository.deleteById(id);
    }

}
