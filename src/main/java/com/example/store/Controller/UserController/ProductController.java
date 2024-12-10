package com.example.store.Controller.UserController;

import com.example.store.Entity.Brand;
import com.example.store.Entity.Category;
import com.example.store.Entity.Product;
import com.example.store.Repository.BrandRepository;
import com.example.store.Repository.CategoryRepository;
import com.example.store.Service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin("*")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BrandRepository brandRepository;


    @GetMapping("/mechanicalWatch")
    public List<Product> getLimitedMechanicalWatch() {
        return productService.getLimitedProductsByCategory(1L, 4);
    }

    // Endpoint để lấy 4 sản phẩm đồng hồ
    @GetMapping("/smartWatches")
    public List<Product> getLimitedSmartWatches() {
        return productService.getLimitedProductsByCategory(3L, 4);
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Product> getProductById(@PathVariable Long productId) {
        Product product = productService.findById(productId);
        if (product != null) {
            return ResponseEntity.ok(product);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Phương thức lấy các sản phẩm cùng loại
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable Long categoryId) {
        List<Product> products = productService.findByCategoryId(categoryId);
        return ResponseEntity.ok(products);
    }


    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    @GetMapping("/brands")
    public ResponseEntity<List<Brand>> getAllBrands() {
        List<Brand> brands = brandRepository.findAll();
        return new ResponseEntity<>(brands, HttpStatus.OK);
    }

    @GetMapping("/brand/{brandId}")
    public ResponseEntity<List<Product>> getProductsByBrand(@PathVariable Long brandId) {
        List<Product> products = productService.findByBrandId(brandId);
        return ResponseEntity.ok(products);
    }

    // Endpoint để lấy tất cả sản phẩm đồng hồ
    @GetMapping("/all-watches")
    public ResponseEntity<List<Product>> getAllWatchProducts() {
        // Giả sử categoryId của "Đồng hồ" là 2
        List<Product> watches = productService.findByCategoryId(2L);
        return ResponseEntity.ok(watches);
    }
}
