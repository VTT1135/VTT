package com.example.store.Service;

import com.example.store.Entity.Promotion;
import com.example.store.Repository.OrderRepository;
import com.example.store.Repository.PromotionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PromotionService {

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private OrderRepository orderRepository;


    // Kiểm tra xem mã khuyến mãi đã được sử dụng bởi người dùng hay chưa
    public boolean isPromoCodeUsedByUser(Long userId, String promoCode) {
        // Kiểm tra nếu tồn tại đơn hàng của user với mã khuyến mãi
        return orderRepository.existsByUserIdAndPromoCode(userId, promoCode);
    }

    public List<Promotion> getAllPromotions() {
        return promotionRepository.findAll();
    }

    public Optional<Promotion> getPromotionById(Long id) {
        return promotionRepository.findById(id);
    }

    public Promotion createPromotion(Promotion promotion) {
        return promotionRepository.save(promotion);
    }

    public Promotion updatePromotion(Long id, Promotion promotionDetails) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));
        promotion.setCode(promotionDetails.getCode());
        promotion.setDiscountPercentage(promotionDetails.getDiscountPercentage());
        promotion.setDiscountAmount(promotionDetails.getDiscountAmount());
        promotion.setStartDate(promotionDetails.getStartDate());
        promotion.setEndDate(promotionDetails.getEndDate());
        return promotionRepository.save(promotion);
    }

    public void deletePromotion(Long id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));
        promotionRepository.delete(promotion);
    }

    // Kiểm tra mã khuyến mãi
    public Optional<Promotion> checkPromotionCode(String code) {
        Optional<Promotion> promotion = promotionRepository.findByCode(code);

        if (promotion.isPresent()) {
            Promotion promo = promotion.get();
            LocalDateTime now = LocalDateTime.now();

            // Kiểm tra xem mã có hiệu lực không (theo ngày bắt đầu và kết thúc)
            if (now.isAfter(promo.getStartDate()) && now.isBefore(promo.getEndDate())) {
                return promotion;
            }
        }

        return Optional.empty();
    }
}

