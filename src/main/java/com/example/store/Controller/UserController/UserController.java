package com.example.store.Controller.UserController;

import com.example.store.Entity.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class UserController {


    @GetMapping("/current-user")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
        }

        // Lấy đối tượng User từ Authentication
        User user = (User) authentication.getPrincipal(); // Cast thành đối tượng User

        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());  // Lấy ID từ User
        response.put("username", user.getUsername()); // Lấy username từ User

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public Map<String, Object> checkLoginStatus(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        response.put("isLoggedIn", authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken));
        return response;
    }

    /*@GetMapping("/info")
    public UserProfileResponse getProfile() {
        // Lấy Authentication từ SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Kiểm tra xem người dùng có đăng nhập hay không
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            throw new RuntimeException("Người dùng chưa đăng nhập hoặc thông tin không hợp lệ");
        }

        // Lấy thông tin người dùng từ Authentication
        User user = (User) authentication.getPrincipal();

        // Trả về thông tin cần thiết
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toSet())
        );
    }*/

}
