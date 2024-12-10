package com.example.store.Entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class OrderItemDTO {
    private Long productId;
    private String productName;
    private int quantity;
    private double price;
    private String imageUrl;


}
