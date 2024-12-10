package com.example.store.Controller.UserController;

import com.example.store.Entity.*;
import com.example.store.Repository.CartRepository;
import com.example.store.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@RestController
@RequestMapping("/api/cart")
@CrossOrigin("*")
public class CartController {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @GetMapping
    public ResponseEntity<List<CartItemDTO>> getCart() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            User user = (User) authentication.getPrincipal();
            Long userId = user.getId();

            List<Cart> cartItems = cartRepository.findAllByUserId(userId);
            List<CartItemDTO> cartItemDTOs = new ArrayList<>();


            for (Cart cart : cartItems) {
                Product product = cart.getProduct();
                CartItemDTO cartItemDTO = new CartItemDTO(
                        cart.getId(),
                        product.getId(),
                        product.getName(),
                        cart.getQuantity(),
                        product.getPrice(),
                        product.getImageUrl()
                );
                cartItemDTOs.add(cartItemDTO);
            }

            return ResponseEntity.ok(cartItemDTOs);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }




    @PostMapping("/add")
    public ResponseEntity<Void> addToCart(@RequestBody CartItemRequest cartItemRequest) {
        Long productId = cartItemRequest.getProductId();
        int quantity = cartItemRequest.getQuantity();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            User user = (User) authentication.getPrincipal();
            Long userId = user.getId();

            try {
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new RuntimeException("Product not found"));

                Optional<Cart> optionalCart = cartRepository.findByUserAndProduct(userId, productId);
                Cart cart;

                if (optionalCart.isPresent()) {
                    cart = optionalCart.get();
                    cart.setQuantity(cart.getQuantity() + quantity);
                } else {
                    cart = new Cart(user, product, quantity);
                }

                cartRepository.save(cart);
                return ResponseEntity.ok().build();
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PutMapping("/update/{cartId}")
    public ResponseEntity<Void> updateCartItem(@PathVariable Long cartId, @RequestParam int quantity) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            Optional<Cart> optionalCart = cartRepository.findById(cartId);
            if (optionalCart.isPresent()) {
                Cart cartItem = optionalCart.get();
                cartItem.setQuantity(quantity);
                cartRepository.save(cartItem);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @DeleteMapping("/remove/{cartId}")
    public ResponseEntity<Void> removeCartItem(@PathVariable Long cartId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            Optional<Cart> optionalCart = cartRepository.findById(cartId);
            if (optionalCart.isPresent()) {
                cartRepository.delete(optionalCart.get());
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }


}
