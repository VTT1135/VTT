package com.example.store.Controller.UserController;

import com.example.store.Entity.*;
import com.example.store.Repository.*;
import com.example.store.Service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@CrossOrigin("*")
public class VNPayController {

    @Value("${vnpay.tmnCode}")
    private String vnp_TmnCode;

    @Value("${vnpay.hashSecret}")
    private String vnp_HashSecret;

    @Value("${vnpay.paymentUrl}")
    private String vnp_PaymentUrl;

    @Value("${vnpay.returnUrl}")
    private String vnp_ReturnUrl;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private PaymentRepository paymentRepository;


    @Autowired
    private PaymentService paymentService;

    @PostMapping("/payment/prepare")
    public ResponseEntity<?> preparePayment(@RequestBody OrderRequest orderRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            User user = (User) authentication.getPrincipal();
            paymentService.saveOrderRequest(user.getId(), orderRequest);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }


    @GetMapping("/payment/vnpay")
    public String createPayment(){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();
            Long userId = user.getId();

            OrderRequest orderRequest = paymentService.getOrderRequest(userId);
            if (orderRequest == null) {
                return "error";
            }

            double amount = orderRequest.getTotalPrice(); // Sử dụng totalPrice từ OrderRequest
            String orderInfo = "Thanh Toán";

            String vnp_Version = "2.1.0";
            String vnp_Command = "pay";
            String orderType = "other";
            String vnp_TxnRef = getRandomNumber(8);
            String vnp_IpAddr = "127.0.0.1";
            String vnp_TmnCode = this.vnp_TmnCode;

            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", vnp_Version);
            vnp_Params.put("vnp_Command", vnp_Command);
            vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
            vnp_Params.put("vnp_Amount", String.valueOf((long) (amount * 100))); // Số tiền phải nhân với 100
            vnp_Params.put("vnp_CurrCode", "VND");
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
            vnp_Params.put("vnp_OrderInfo", orderInfo);
            vnp_Params.put("vnp_OrderType", orderType);
            vnp_Params.put("vnp_Locale", "vn");
            vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
            vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnp_CreateDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

            cld.add(Calendar.MINUTE, 15);
            String vnp_ExpireDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

            // Tạo chuỗi dữ liệu để mã hóa
            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
            Collections.sort(fieldNames);
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();
            for (String fieldName : fieldNames) {
                String fieldValue = vnp_Params.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    // Build hash data
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    // Build query
                    query.append(fieldName);
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                    query.append('&');
                    hashData.append('&');
                }
            }
            // Xóa ký tự & cuối cùng
            query.deleteCharAt(query.length() - 1);
            hashData.deleteCharAt(hashData.length() - 1);

            // Mã hóa chuỗi dữ liệu bằng MD5 hoặc SHA256 với HashSecret
            String vnp_SecureHash = VNPayUtils.hmacSHA512(vnp_HashSecret, hashData.toString());
            query.append("&vnp_SecureHash=");
            query.append(vnp_SecureHash);

            String paymentUrl = vnp_PaymentUrl + "?" + query.toString();
            return "redirect:" + paymentUrl;

        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    private String getRandomNumber(int len) {
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append((char) ('0' + rnd.nextInt(10)));
        }
        return sb.toString();
    }

    @GetMapping("/payment/vnpay-return")
    public String vnPayReturn(@RequestParam Map<String, String> requestParams) throws Exception {
        String vnp_SecureHash = requestParams.get("vnp_SecureHash");
        requestParams.remove("vnp_SecureHash");

        // Tạo lại chuỗi dữ liệu mã hóa
        List<String> fieldNames = new ArrayList<>(requestParams.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = requestParams.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                hashData.append('&');
            }
        }
        hashData.deleteCharAt(hashData.length() - 1);

        // So sánh hash dữ liệu
        String hashCheck = VNPayUtils.hmacSHA512(vnp_HashSecret, hashData.toString());
        String responseCode = requestParams.get("vnp_ResponseCode"); // Lấy mã phản hồi

        // Kiểm tra mã phản hồi
        if (hashCheck.equals(vnp_SecureHash) && "00".equals(responseCode)) {
            // Thanh toán hợp lệ, xử lý lưu đơn hàng tại đây
            return placeOrderAfterPayment(); // Gọi hàm lưu đơn hàng sau khi thanh toán
        } else {
            // Thanh toán không hợp lệ hoặc bị hủy
            return "views/paymentFailed";
        }
    }

    public String placeOrderAfterPayment() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            User user = (User) authentication.getPrincipal();
            Long userId = user.getId();

            // Lấy thông tin đơn hàng đã lưu
            OrderRequest orderRequest = paymentService.getOrderRequest(userId);
            if (orderRequest == null) {
                return "views/paymentFailed";
            }

            List<Cart> selectedCartItems = cartRepository.findAllByIdIn(orderRequest.getSelectedCartIds());
            if (selectedCartItems.isEmpty()) {
                return "views/paymentFailed";
            }

            try {
                // Lấy thông tin người dùng
                User existingUser = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("User not found"));

                // Khởi tạo đơn hàng với thông tin từ OrderRequest
                Order order = new Order();
                order.setUser(existingUser);
                order.setOrderDate(LocalDateTime.now());
                order.setStatus("Pending");
                order.setTotalPrice(orderRequest.getTotalPrice());
                order.setRecipientName(orderRequest.getRecipientName());
                order.setPhoneNumber(orderRequest.getPhoneNumber());
                order.setAddress(orderRequest.getAddress());
                order.setPromoCode(orderRequest.getPromoCode());

                Order savedOrder = orderRepository.save(order);

                // Lưu các mục đơn hàng đã chọn
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

                // Chỉ xóa các sản phẩm đã chọn
                cartRepository.deleteAll(selectedCartItems);

                // Lưu thông tin thanh toán
                Payment payment = new Payment();
                payment.setOrder(savedOrder);
                payment.setPaymentMethod("VNPAY");
                payment.setStatus("Đã thanh toán");
                paymentRepository.save(payment);

                // Xóa dữ liệu đã lưu
                paymentService.clearUserData(userId);

                return "views/paymentSuccess";
            } catch (Exception e) {
                return "views/paymentFailed";
            }
        }
        return "views/paymentFailed";
    }

}






