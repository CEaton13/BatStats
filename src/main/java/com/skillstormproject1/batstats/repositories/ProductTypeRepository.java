package com.skillstormproject1.batstats.repositories;

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
}
