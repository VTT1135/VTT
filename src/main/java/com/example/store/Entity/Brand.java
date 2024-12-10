package com.example.store.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "brands")
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 2, max = 100, message = "Tên thương hiệu phải từ 2 đến 100 ký tự")
    @Column(nullable = false, unique = true)
    private String name;

    @Size(max = 255, message = "Đường dẫn hình ảnh không quá 255 ký tự")
    @Column(name = "image_url")
    private String imageUrl;
}
