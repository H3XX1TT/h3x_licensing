package org.h3x_licensing.product;

import org.h3x_licensing.web.dto.ProductForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public Product createProduct(ProductForm form) {
        String normalizedSlug = normalizeSlug(form.slug());
        if (productRepository.existsBySlugIgnoreCase(normalizedSlug)) {
            throw new IllegalArgumentException("A product with this slug already exists.");
        }

        Product product = new Product();
        product.setName(form.name().trim());
        product.setSlug(normalizedSlug);
        product.setDescription(form.description().trim());
        product.setActive(true);
        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public Product getRequiredById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));
    }

    @Transactional(readOnly = true)
    public Product getRequiredBySlug(String slug) {
        return productRepository.findBySlugIgnoreCase(slug)
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));
    }

    @Transactional(readOnly = true)
    public List<Product> listAll() {
        return productRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public long countProducts() {
        return productRepository.count();
    }

    private String normalizeSlug(String value) {
        return value.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }
}

