package com.example.store.Controller.AdminController;

import com.example.store.Entity.Order;
import com.example.store.Service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/api/orders")
@CrossOrigin("*")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    // API lấy tất cả đơn hàng
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    // API lấy đơn hàng theo status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable String status) {
        List<Order> orders;
        if ("all".equalsIgnoreCase(status)) {
            orders = orderService.getAllOrders();
        } else {
            orders = orderService.getOrdersByStatus(status);
        }
        return ResponseEntity.ok(orders);
    }


    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderDetails(@PathVariable String id) {
        Order order = orderService.getOrderById(id);
        if (order != null) {
            return ResponseEntity.ok(order);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable String id, @RequestBody Map<String, String> statusRequest) {
        String status = statusRequest.get("status");

        // Lấy đơn hàng từ service
        Order order = orderService.getOrderById(id);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        // Xử lý các trạng thái khác nhau
        if ("Confirmed".equalsIgnoreCase(status)) {
            order.setStatus("Confirmed");
        } else if ("Canceled".equalsIgnoreCase(status)) {
            order.setStatus("Canceled");
        } else if ("Completed".equalsIgnoreCase(status)) { // Thêm trạng thái Completed
            if (!"Confirmed".equalsIgnoreCase(order.getStatus())) {
                return ResponseEntity.badRequest()
                        .body(null); // Chỉ cho phép cập nhật Completed nếu đơn hàng đã được Confirmed
            }
            order.setStatus("Completed");
        } else {
            return ResponseEntity.badRequest().body(null);
        }

        // Lưu thay đổi
        orderService.saveOrder(order);

        // Trả về đơn hàng đã cập nhật
        return ResponseEntity.ok(order);
    }


    // API lấy đơn hàng theo ngày đặt
    @GetMapping("/date/{date}")
    public ResponseEntity<List<Order>> getOrdersByDate(
            @PathVariable @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate date) {
        // Convert to Vietnam timezone
        ZoneId vietnamZone = ZoneId.of("Asia/Ho_Chi_Minh");
        LocalDate vietnamDate = date.atStartOfDay(ZoneOffset.UTC)
                .withZoneSameInstant(vietnamZone)
                .toLocalDate();

        List<Order> orders = orderService.getOrdersByDate(vietnamDate);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/range/{startDate}/{endDate}")
    public ResponseEntity<List<Order>> getOrdersByDateRange(
            @PathVariable @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate startDate,
            @PathVariable @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate endDate) {

        // Convert to Vietnam timezone
        ZoneId vietnamZone = ZoneId.of("Asia/Ho_Chi_Minh");
        LocalDateTime startDateTime = startDate.atStartOfDay()
                .atZone(ZoneOffset.UTC)
                .withZoneSameInstant(vietnamZone)
                .toLocalDateTime();

        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX)
                .atZone(ZoneOffset.UTC)
                .withZoneSameInstant(vietnamZone)
                .toLocalDateTime();

        List<Order> orders = orderService.getOrdersByDateRange(startDateTime, endDateTime);
        return ResponseEntity.ok(orders);
    }

    // API lấy đơn hàng theo tháng
    @GetMapping("/month/{year}/{month}")
    public ResponseEntity<List<Order>> getOrdersByMonth(@PathVariable int year, @PathVariable int month) {
        List<Order> orders = orderService.getOrdersByMonth(year, month);
        return ResponseEntity.ok(orders);
    }

}
