package com.example.store.Entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TopPurchasedProducts {
    private Long productId;
    private String productName;
    private Long totalQuantitySold;

    // Constructor
    public TopPurchasedProducts(Long productId, String productName, Long totalQuantitySold) {
        this.productId = productId;
        this.productName = productName;
        this.totalQuantitySold = totalQuantitySold;
    }
}
