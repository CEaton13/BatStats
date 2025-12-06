package com.skillstormproject1.batstats.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.skillstormproject1.batstats.models.ProductType;

@Repository     // Repository Interface for ProductType
public interface ProductTypeRepository extends JpaRepository<ProductType, Integer> {
    /**
     *  Services are going to depend on the Repos
     *      findByName
     *      findBy*
     *      check if a product exists
     */

    // finds a product by name
    Optional<ProductType> findByName(String Name);

    // finds all product types of the specified category
    List<ProductType> findByCategory(String category);

    // check if a product type exists by the name provided
    boolean existsByName(String name);
}
