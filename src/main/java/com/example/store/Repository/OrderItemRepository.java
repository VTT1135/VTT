package com.example.store.Repository;

import com.example.store.Entity.MonthlyProductSoldCount;
import com.example.store.Entity.OrderItem;
import com.example.store.Entity.TopPurchasedProducts;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    @Query("SELECT new com.example.store.Entity.MonthlyProductSoldCount(MONTH(oi.order.orderDate), SUM(oi.quantity)) " +
            "FROM OrderItem oi JOIN oi.order o WHERE o.status = 'COMPLETED' AND YEAR(o.orderDate) = :year " +
            "GROUP BY MONTH(oi.order.orderDate) ORDER BY MONTH(oi.order.orderDate)")
    List<MonthlyProductSoldCount> findMonthlyProductSoldCountForCompletedOrders(@Param("year") int year);

    @Query("SELECT new com.example.store.Entity.TopPurchasedProducts(oi.product.id, oi.product.name, SUM(oi.quantity)) " +
            "FROM OrderItem oi JOIN oi.order o " +
            "WHERE o.status = 'COMPLETED' " +
            "GROUP BY oi.product.id, oi.product.name " +
            "ORDER BY SUM(oi.quantity) DESC")
    List<TopPurchasedProducts> findTopPurchasedProducts(@Param("limit") int limit, Pageable pageable);



}
