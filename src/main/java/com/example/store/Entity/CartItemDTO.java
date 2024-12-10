package com.example.store.Entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItemDTO {
    private Long id;
    private Long productId;
    private String productName;
    private int quantity;
    private double price;
    private String imageUrl;

    public CartItemDTO(Long id,
                       Long productId,
                       String productName,
                       int quantity,
                       double price,
                       String imageUrl) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
        this.imageUrl = imageUrl;
    }
}