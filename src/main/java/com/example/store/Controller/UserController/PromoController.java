package com.example.store.Controller.UserController;

import com.example.store.Entity.Promotion;
import com.example.store.Entity.User;
import com.example.store.Service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/promo")
@CrossOrigin("*")
public class PromoController {

    @Autowired
    private PromotionService promotionService;

    @PostMapping("/check")
    public ResponseEntity<Map<String, Object>> checkPromoCode(@RequestBody Map<String, String> request) {
        String promoCode = request.get("promoCode");

        // Lấy thông tin người dùng hiện tại
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = null;
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();
            userId = user.getId(); // Lấy ID người dùng
        }

        Optional<Promotion> promotion = promotionService.checkPromotionCode(promoCode);

        Map<String, Object> response = new HashMap<>();
        if (promotion.isPresent()) {
            Promotion promo = promotion.get();

            // Kiểm tra xem mã đã được sử dụng bởi người dùng chưa
            if (promotionService.isPromoCodeUsedByUser(userId, promoCode)) {
                response.put("valid", false);
                response.put("message", "Bạn đã sử dụng mã khuyến mại này.");
            } else {
                response.put("valid", true);
                response.put("discountAmount", promo.getDiscountAmount());
                response.put("discountPercentage", promo.getDiscountPercentage());
            }
            return ResponseEntity.ok(response);
        } else {
            response.put("valid", false);
            response.put("message", "Mã khuyến mãi không hợp lệ.");
            return ResponseEntity.ok(response);
        }
    }

}


