package com.example.store.Controller.AdminController;

import com.example.store.Entity.Brand;
import com.example.store.Entity.Category;
import com.example.store.Entity.Product;
import com.example.store.Entity.ProductImage;
import com.example.store.Repository.BrandRepository;
import com.example.store.Repository.CategoryRepository;
import com.example.store.Service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/admin/api/products")
@CrossOrigin("*")
public class AdminProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Value("${upload.path}")
    private String uploadPath;


    @PostMapping
    public ResponseEntity<Product> createProduct(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") double price,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam("brandId") Long brandId,
            @RequestParam("images") List<MultipartFile> images) {

        // Tạo sản phẩm
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);

        // Gán Category và Brand
        Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
        if (categoryOpt.isPresent()) {
            product.setCategory(categoryOpt.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        Optional<Brand> brandOpt = brandRepository.findById(brandId);
        if (brandOpt.isPresent()) {
            product.setBrand(brandOpt.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        // Lưu ảnh
        List<ProductImage> productImages = new ArrayList<>();
        for (MultipartFile image : images) {
            String imageUrl = saveImage(image); // Hàm lưu file ảnh
            ProductImage productImage = new ProductImage();
            productImage.setImageUrl(imageUrl);
            productImage.setProduct(product);
            productImages.add(productImage);
        }
        product.setImages(productImages);

        return new ResponseEntity<>(productService.saveProduct(product), HttpStatus.CREATED);
    }



    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Optional<Product> productOpt = productService.getProductById(id);
        if (productOpt.isPresent()) {
            return ResponseEntity.ok(productOpt.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") double price,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam("brandId") Long brandId,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @RequestParam(value = "deletedImages", required = false) List<String> deletedImages) {

        Optional<Product> productOpt = productService.getProductById(id);
        if (!productOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        Product product = productOpt.get();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);

        // Xóa ảnh cũ nếu có ảnh mới được upload
        if (images != null && !images.isEmpty()) {
            for (ProductImage image : product.getImages()) {
                deleteOldImage(image.getImageUrl());
            }
            product.getImages().clear();

            // Lưu ảnh mới
            for (MultipartFile image : images) {
                String imageUrl = saveImage(image);
                ProductImage productImage = new ProductImage();
                productImage.setImageUrl(imageUrl);
                productImage.setProduct(product);
                product.getImages().add(productImage);
            }
        }

        // Xóa các ảnh đã được đánh dấu để xóa
        if (deletedImages != null && !deletedImages.isEmpty()) {
            for (String imageUrl : deletedImages) {
                deleteOldImage(imageUrl); // Xóa ảnh từ server
                product.getImages().removeIf(image -> image.getImageUrl().equals(imageUrl));
            }
        }

        // Xử lý Category và Brand
        Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
        categoryOpt.ifPresent(product::setCategory);

        Optional<Brand> brandOpt = brandRepository.findById(brandId);
        brandOpt.ifPresent(product::setBrand);

        return new ResponseEntity<>(productService.saveProduct(product), HttpStatus.OK);
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteProduct(@PathVariable Long id) {
        Optional<Product> productOpt = productService.getProductById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();

            // Xóa ảnh
            for (ProductImage image : product.getImages()) {
                deleteOldImage(image.getImageUrl());
            }

            productService.deleteProduct(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    private void deleteOldImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // Nếu imageUrl là URL đầy đủ, trích xuất tên tệp từ URL
            String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);

            // Tạo đường dẫn đầy đủ đến ảnh
            Path imagePath = Paths.get(uploadPath, fileName);

            try {
                Files.deleteIfExists(imagePath); // Xóa ảnh nếu tồn tại
            } catch (IOException e) {
                throw new RuntimeException("Could not delete old image " + fileName, e);
            }
        }
    }


    private String saveImage(MultipartFile image) {
        try {
            String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
            Path uploadDir = Paths.get(uploadPath);

            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            Path filePath = uploadDir.resolve(fileName);
            Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Could not store file " + image.getOriginalFilename(), e);
        }
    }
}
