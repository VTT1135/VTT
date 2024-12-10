package com.example.store.Controller.UserController;

import com.example.store.Entity.*;
import com.example.store.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/order")
@CrossOrigin("*")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private PaymentRepository paymentRepository;


    @PostMapping("/place")
    public ResponseEntity<Void> placeOrder(@RequestBody OrderRequest orderRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            User user = (User) authentication.getPrincipal();
            Long userId = user.getId();

            List<Cart> selectedCartItems = cartRepository.findAllByIdIn(orderRequest.getSelectedCartIds());

            if (selectedCartItems.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            boolean allItemsBelongToUser = selectedCartItems.stream()
                    .allMatch(cart -> cart.getUser().getId().equals(userId));

            if (!allItemsBelongToUser) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            try {
                Order order = new Order();
                order.setUser(user);
                order.setOrderDate(LocalDateTime.now());
                order.setStatus("Pending");
                order.setTotalPrice(orderRequest.getTotalPrice());
                order.setPromoCode(orderRequest.getPromoCode());
                order.setRecipientName(orderRequest.getRecipientName());
                order.setPhoneNumber(orderRequest.getPhoneNumber());
                order.setAddress(orderRequest.getAddress());

                Order savedOrder = orderRepository.save(order);

                List<OrderItem> orderItems = new ArrayList<>();
                for (Cart cartItem : selectedCartItems) {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(savedOrder);
                    orderItem.setProduct(cartItem.getProduct());
                    orderItem.setQuantity(cartItem.getQuantity());
                    orderItem.setPrice(cartItem.getProduct().getPrice());
                    orderItems.add(orderItem);
                }
                orderItemRepository.saveAll(orderItems);

                cartRepository.deleteAll(selectedCartItems);

                Payment payment = new Payment();
                payment.setOrder(savedOrder);
                payment.setPaymentMethod(orderRequest.getPaymentMethod());
                if ("COD".equals(orderRequest.getPaymentMethod())) {
                    payment.setStatus("Thanh toán khi nhận hàng");
                }
                paymentRepository.save(payment);

                return ResponseEntity.status(HttpStatus.CREATED).build();
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }


    @GetMapping("/history")
    public ResponseEntity<List<OrderDTO>> getOrderHistory() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            User user = (User) authentication.getPrincipal();
            Long userId = user.getId();

            List<Order> orders = orderRepository.findByUserId(userId);
            List<OrderDTO> orderDTOs = new ArrayList<>();

            for (Order order : orders) {
                Payment payment = paymentRepository.findByOrderId(order.getId());
                OrderDTO orderDTO = new OrderDTO(
                        order.getId(),
                        order.getOrderDate(),
                        order.getStatus(),
                        order.getTotalPrice(),
                        order.getRecipientName(),
                        order.getPhoneNumber(),
                        order.getAddress(),
                        payment != null ? payment.getPaymentMethod() : null,
                        payment != null ? payment.getStatus() : null,
                        order.getPromoCode()
                );
                orderDTOs.add(orderDTO);
            }

            return ResponseEntity.ok(orderDTOs);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }



    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> getOrderDetails(@PathVariable String orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            Optional<Order> orderOptional = orderRepository.findById(orderId);
            if (orderOptional.isPresent()) {
                Order order = orderOptional.get();
                // Lấy thông tin thanh toán từ bảng payments
                Payment payment = paymentRepository.findByOrderId(order.getId());

                // Chuyển đổi Order sang OrderDTO
                OrderDTO orderDTO = new OrderDTO();
                orderDTO.setId(order.getId());
                orderDTO.setOrderDate(order.getOrderDate());
                orderDTO.setStatus(order.getStatus());
                orderDTO.setTotalPrice(order.getTotalPrice());
                orderDTO.setRecipientName(order.getRecipientName());
                orderDTO.setPhoneNumber(order.getPhoneNumber());
                orderDTO.setAddress(order.getAddress());
                orderDTO.setPaymentMethod(payment != null ? payment.getPaymentMethod() : null);
                orderDTO.setPaymentStatus(payment != null ? payment.getStatus() : null);
                orderDTO.setPromoCode(order.getPromoCode());

                // Chuyển đổi danh sách OrderItem sang OrderItemDTO
                List<OrderItemDTO> orderItemDTOs = order.getOrderItems().stream().map(orderItem -> {
                    OrderItemDTO dto = new OrderItemDTO();
                    dto.setProductId(orderItem.getProduct().getId());
                    dto.setProductName(orderItem.getProduct().getName());
                    dto.setImageUrl(orderItem.getProduct().getImageUrl());
                    dto.setQuantity(orderItem.getQuantity());
                    dto.setPrice(orderItem.getPrice());
                    return dto;
                }).toList();

                orderDTO.setItems(orderItemDTOs);

                return ResponseEntity.ok(orderDTO);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
    @PatchMapping("/cancel/{orderId}")
    public ResponseEntity<Void> cancelOrder(@PathVariable String orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            User user = (User) authentication.getPrincipal();
            Long userId = user.getId();

            Optional<Order> orderOptional = orderRepository.findById(orderId);

            if (orderOptional.isPresent()) {
                Order order = orderOptional.get();

                if (!order.getUser().getId().equals(userId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }

                if ("Pending".equals(order.getStatus()) || "Processing".equals(order.getStatus())) {
                    order.setStatus("Canceled");
                    orderRepository.save(order);
                    return ResponseEntity.ok().build();
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }


    @GetMapping("/{orderId}/items")
    public ResponseEntity<List<OrderItemDTO>> getOrderItems(@PathVariable String orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            User user = (User) authentication.getPrincipal();
            Long userId = user.getId();

            // Lấy đơn hàng từ repository
            Optional<Order> orderOptional = orderRepository.findById(orderId);

            if (orderOptional.isPresent()) {
                Order order = orderOptional.get();

                // Kiểm tra xem đơn hàng có thuộc về người dùng hiện tại không
                if (!order.getUser().getId().equals(userId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
                }

                // Lấy danh sách OrderItems và chuyển đổi thành OrderItemDTO
                List<OrderItemDTO> orderItemDTOs = order.getOrderItems().stream().map(orderItem -> {
                    OrderItemDTO dto = new OrderItemDTO();
                    dto.setProductId(orderItem.getProduct().getId());
                    dto.setProductName(orderItem.getProduct().getName());
                    dto.setImageUrl(orderItem.getProduct().getImageUrl());
                    dto.setQuantity(orderItem.getQuantity());
                    dto.setPrice(orderItem.getPrice());
                    return dto;
                }).toList();

                // Trả về danh sách OrderItemDTO
                return ResponseEntity.ok(orderItemDTOs);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }




}
