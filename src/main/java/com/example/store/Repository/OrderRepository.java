package com.example.store.Repository;

import com.example.store.Entity.MonthlyOrderCount;
import com.example.store.Entity.MonthlyRevenue;
import com.example.store.Entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByUserId(Long userId);

    // Kiểm tra nếu tồn tại đơn hàng của một user với mã khuyến mãi
    boolean existsByUserIdAndPromoCode(Long userId, String promoCode);

    @Query("SELECT new com.example.store.Entity.MonthlyRevenue(MONTH(o.orderDate), SUM(o.totalPrice)) " +
            "FROM Order o WHERE o.status = 'COMPLETED' AND YEAR(o.orderDate) = :year " +
            "GROUP BY MONTH(o.orderDate) ORDER BY MONTH(o.orderDate)")
    List<MonthlyRevenue> findMonthlyRevenueForCompletedOrders(@Param("year") int year);


    @Query("SELECT new com.example.store.Entity.MonthlyOrderCount(MONTH(o.orderDate), COUNT(o.id)) " +
            "FROM Order o WHERE o.status = 'COMPLETED' AND YEAR(o.orderDate) = :year " +
            "GROUP BY MONTH(o.orderDate) ORDER BY MONTH(o.orderDate)")
    List<MonthlyOrderCount> findMonthlyOrderCountForCompletedOrders(@Param("year") int year);


    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE o.status = 'Completed' AND o.orderDate BETWEEN :startDate AND :endDate")
    Double findRevenueByDate(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    List<Order> findByStatus(String status);

    // Lọc đơn hàng theo ngày đặt
    @Query("SELECT o FROM Order o WHERE DATE(o.orderDate) = :date")
    List<Order> findByOrderDate(@Param("date") LocalDate date);

    // Lọc đơn hàng theo tháng
    @Query("SELECT o FROM Order o WHERE YEAR(o.orderDate) = :year AND MONTH(o.orderDate) = :month")
    List<Order> findByMonth(int year, int month);

    List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}
