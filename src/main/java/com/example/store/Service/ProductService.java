package com.example.store.Service;

import com.example.store.Entity.Product;
import com.example.store.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }


    public List<Product> getLimitedProductsByCategory(Long categoryId, int limit) {
        List<Product> products = productRepository.findByCategoryId(categoryId);
        return products.stream().limit(limit).collect(Collectors.toList());
    }


    public Product findById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    public List<Product> findByCategoryId(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    public List<Product> findByBrandId(Long brandId) {
        return productRepository.findByBrandId(brandId);
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

}
