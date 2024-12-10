package com.example.store.Controller.AdminController;

import com.example.store.Entity.Brand;
import com.example.store.Repository.BrandRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/admin/api/brands")
@CrossOrigin("*")
public class AdminBrandController {

    @Autowired
    private BrandRepository brandRepository;

    @Value("${upload.path}")
    private String uploadPath;

    // Get all brands
    @GetMapping
    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }

    // Get brand by ID
    @GetMapping("/{id}")
    public ResponseEntity<Brand> getBrandById(@PathVariable Long id) {
        Optional<Brand> brand = brandRepository.findById(id);
        return brand.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // Create a new brand with image
    @PostMapping
    public ResponseEntity<Brand> createBrand(
            @RequestParam("name") String name,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        String imageUrl = null;
        if (image != null) {
            imageUrl = saveImage(image);
        }

        Brand brand = new Brand();
        brand.setName(name);
        brand.setImageUrl(imageUrl);

        return new ResponseEntity<>(brandRepository.save(brand), HttpStatus.CREATED);
    }

    // Update an existing brand with image
    @PutMapping("/{id}")
    public ResponseEntity<Brand> updateBrand(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        return brandRepository.findById(id).map(brand -> {
            brand.setName(name);

            if (image != null) {
                deleteOldImage(brand.getImageUrl());
                String imageUrl = saveImage(image);
                brand.setImageUrl(imageUrl);
            }

            return ResponseEntity.ok(brandRepository.save(brand));
        }).orElse(ResponseEntity.notFound().build());
    }

    // Delete a brand
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteBrand(@PathVariable Long id) {
        return brandRepository.findById(id).map(brand -> {
            deleteOldImage(brand.getImageUrl());
            brandRepository.delete(brand);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
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


    // Save image to server
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
