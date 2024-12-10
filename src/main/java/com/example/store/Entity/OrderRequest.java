package com.example.store.Entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderRequest {
    private List<Long> selectedCartIds;
    private Long userId;
    private String recipientName;
    private String phoneNumber;
    private String address;
    private double totalPrice;
    private String promoCode;
    private String paymentMethod;
    private List<CartItemRequest> items;
}
