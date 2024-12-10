package com.example.store.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.example.store.Entity.Order;
import com.example.store.Repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status);
    }

    public Order getOrderById(String id) {
        return orderRepository.findById(id).orElse(null);
    }

    public void saveOrder(Order order) {
        orderRepository.save(order);
    }

    // Lấy đơn hàng theo ngày
    public List<Order> getOrdersByDate(LocalDate date) {
        return orderRepository.findByOrderDate(date);
    }


    public List<Order> getOrdersByMonth(int year, int month) {
        return orderRepository.findByMonth(year, month);
    }

    public List<Order> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByOrderDateBetween(startDate, endDate);
    }

}
