package com.example.store.Entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor // Thêm annotation để tạo constructor mặc định
public class OrderDTO {
    private String id;
    private LocalDateTime orderDate;
    private String status;
    private double totalPrice;
    private String recipientName;
    private String phoneNumber;
    private String address;
    private String paymentMethod;
    private String paymentStatus;
    private String promoCode;
    private List<OrderItemDTO> items;

    // Constructor, getters và setters
    public OrderDTO(String id, LocalDateTime orderDate, String status, double totalPrice, String recipientName, String phoneNumber, String address, String paymentMethod, String paymentStatus, String promoCode) {
        this.id = id;
        this.orderDate = orderDate;
        this.status = status;
        this.totalPrice = totalPrice;
        this.recipientName = recipientName;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.promoCode = promoCode;
    }

    // Các phương thức getter và setter khác
}

