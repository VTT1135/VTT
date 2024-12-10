package com.example.store.Repository;

import com.example.store.Entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {


    @Query("SELECT c FROM Cart c WHERE c.user.id = ?1 AND c.product.id = ?2")
    Optional<Cart> findByUserAndProduct(Long userId, Long productId);

    @Query("SELECT c FROM Cart c WHERE c.user.id = ?1")
    List<Cart> findAllByUserId(Long userId);

    @Query("SELECT COALESCE(SUM(c.quantity * c.product.price), 0.0) FROM Cart c WHERE c.user.id = ?1")
    double getTotalAmountByUserId(Long userId);


    List<Cart> findAllByIdIn(List<Long> ids);
}
