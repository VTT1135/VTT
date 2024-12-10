package com.example.store.Service;

import com.example.store.Entity.OrderRequest;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PaymentService {
    private Map<Long, OrderRequest> userOrderRequests = new ConcurrentHashMap<>();

    public void saveOrderRequest(Long userId, OrderRequest orderRequest) {
        userOrderRequests.put(userId, orderRequest);
    }

    public OrderRequest getOrderRequest(Long userId) {
        return userOrderRequests.get(userId);
    }

    public void clearUserData(Long userId) {
        userOrderRequests.remove(userId);
    }
}