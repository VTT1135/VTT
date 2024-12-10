package com.example.store.Entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 2, max = 150, message = "Tên sản phẩm phải từ 2 đến 150 ký tự")
    @Column(nullable = false)
    private String name;

    @Size(max = 1000, message = "Mô tả sản phẩm không quá 1000 ký tự")
    private String description;

    @Column(nullable = false)
    private double price;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ProductImage> images;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @OneToMany(mappedBy = "product")
    @JsonManagedReference
    private List<Comment> comments;

    // Thêm phương thức getImageUrl để trả về ảnh đầu tiên
    public String getImageUrl() {
        if (images != null && !images.isEmpty()) {
            String fileName = images.get(0).getImageUrl(); // Lấy URL của ảnh đầu tiên
            return fileName; // Trích xuất file name từ URL
        }
        return "default_image_url";  // Trả về URL mặc định nếu không có ảnh
    }
}
