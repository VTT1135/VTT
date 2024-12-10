package com.example.store.Repository;

import com.example.store.Entity.Comment;
import com.example.store.Entity.Product;
import com.example.store.Entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByUserAndProduct(User user, Product product);
    Page<Comment> findByProductId(Long productId, Pageable pageable);
    @Query("SELECT c.rating, COUNT(c) FROM Comment c WHERE c.product.id = :productId GROUP BY c.rating")
    List<Object[]> countRatingsByProductId(@Param("productId") Long productId);
}
