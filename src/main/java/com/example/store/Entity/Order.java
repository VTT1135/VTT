package com.example.store.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @Column(name = "id", nullable = false, updatable = false, length = 50, unique = true)
    private String id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "promo_code", length = 50)
    private String promoCode;

    @Column(name = "total_price", nullable = false)
    private double totalPrice;

    @Column(name = "recipient_name", nullable = false, length = 100)
    private String recipientName;

    @Column(name = "phone_number", nullable = false, length = 50)
    private String phoneNumber;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<OrderItem> orderItems;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private Payment payment;

    @PrePersist
    public void prePersist() {
        if (id == null || id.isEmpty()) {
            this.id = generateOrderId();
        }
        if (orderDate == null) {
            orderDate = LocalDateTime.now();
        }
    }
    private String generateOrderId() {
        return "ORD-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
