package com.example.store.Controller.AdminController;

import com.example.store.Entity.MonthlyOrderCount;
import com.example.store.Entity.MonthlyProductSoldCount;
import com.example.store.Entity.MonthlyRevenue;
import com.example.store.Entity.TopPurchasedProducts;
import com.example.store.Repository.OrderItemRepository;
import com.example.store.Repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/admin/api/statistics")
@CrossOrigin("*")
public class AdminStatisticsController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @GetMapping("/monthly-revenue")
    public List<MonthlyRevenue> getMonthlyRevenue(@RequestParam int year) {
        return orderRepository.findMonthlyRevenueForCompletedOrders(year);
    }

    @GetMapping("/monthly-order-count")
    public List<MonthlyOrderCount> getMonthlyOrderCount(@RequestParam int year) {
        return orderRepository.findMonthlyOrderCountForCompletedOrders(year);
    }

    @GetMapping("/monthly-product-sold")
    public List<MonthlyProductSoldCount> getMonthlyProductSold(@RequestParam int year) {
        return orderItemRepository.findMonthlyProductSoldCountForCompletedOrders(year);
    }

    @GetMapping("/top-purchased-products")
    public List<TopPurchasedProducts> getTopPurchasedProducts(@RequestParam int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return orderItemRepository.findTopPurchasedProducts(limit, pageable);
    }


    @GetMapping("/revenue-by-date")
    public Double getRevenueByDate(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        ZoneId vietnamZone = ZoneId.of("Asia/Ho_Chi_Minh");

        LocalDateTime startDateTime = LocalDate.parse(startDate)
                .atStartOfDay()
                .atZone(vietnamZone)
                .toLocalDateTime();

        LocalDateTime endDateTime = LocalDate.parse(endDate)
                .atTime(LocalTime.MAX)
                .atZone(vietnamZone)
                .toLocalDateTime();

        return orderRepository.findRevenueByDate(startDateTime, endDateTime);
    }

}

