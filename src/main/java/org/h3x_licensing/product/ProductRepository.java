package org.h3x_licensing.product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySlugIgnoreCase(String slug);

    Optional<Product> findBySlugIgnoreCase(String slug);

    List<Product> findAllByOrderByCreatedAtDesc();
}

