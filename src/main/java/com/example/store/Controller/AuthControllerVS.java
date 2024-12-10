/*
package com.example.store.Controller;

import com.example.store.Entity.Role;
import com.example.store.Entity.User;
import com.example.store.Repository.RoleRepository;
import com.example.store.Repository.UserRepository;
import com.example.store.Service.EmailService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class AuthControllerVS {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    // API để lấy thông tin người dùng hiện tại
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();
            Optional<User> userOpt = userRepository.findById(user.getId());
            if (userOpt.isPresent()) {
                return ResponseEntity.ok(userOpt.get());
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Người dùng chưa đăng nhập.");
    }

    // API để cập nhật thông tin người dùng
    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(@RequestBody User updatedUser) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof User) {
            User currentUser = (User) authentication.getPrincipal();
            Optional<User> userOpt = userRepository.findById(currentUser.getId());

            if (userOpt.isPresent()) {
                User existingUser = userOpt.get();

                if (updatedUser.getUsername() != null && !updatedUser.getUsername().isEmpty()) {
                    existingUser.setUsername(updatedUser.getUsername());
                }

                if (updatedUser.getEmail() != null && !updatedUser.getEmail().isEmpty()) {
                    existingUser.setEmail(updatedUser.getEmail());
                }

                if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                    existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
                    emailService.sendPasswordChangeEmail(existingUser.getEmail());
                }

                userRepository.save(existingUser);
                return ResponseEntity.ok("Cập nhật thông tin thành công.");
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Người dùng không tồn tại.");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Người dùng chưa đăng nhập.");
    }

    // API đăng ký người dùng
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email đã được sử dụng.");
        }

        if (!user.getPassword().equals(user.getConfirmPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Mật khẩu không trùng khớp.");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEmailVerified(false);
        user.setVerificationCode(generateVerificationCode());

        boolean isFirstUser = userRepository.count() == 0;
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò USER."));
        user.getRoles().add(userRole);

        if (isFirstUser) {
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò ADMIN."));
            user.getRoles().add(adminRole);
        }

        userRepository.save(user);
        emailService.sendVerificationEmail(user.getEmail(), user.getVerificationCode());

        return ResponseEntity.status(HttpStatus.CREATED).body("Đăng ký thành công. Vui lòng kiểm tra email để xác minh.");
    }

    // API xác minh email
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam("code") String code) {
        Optional<User> userOpt = userRepository.findByVerificationCode(code);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setEmailVerified(true);
            user.setVerificationCode(null);
            userRepository.save(user);
            return ResponseEntity.ok("Xác minh email thành công.");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Mã xác minh không hợp lệ hoặc đã hết hạn.");
    }

    // API đăng nhập
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            User user = (User) authentication.getPrincipal();
            return ResponseEntity.ok("Đăng nhập thành công.");
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email hoặc mật khẩu không đúng.");
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // API yêu cầu quên mật khẩu
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam("email") String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (!userOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email không tồn tại.");
        }

        User user = userOpt.get();
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        userRepository.save(user);

        String resetUrl = "http://localhost:8080/reset-password?token=" + token;
        emailService.sendResetPasswordEmail(user.getEmail(), resetUrl);
        return ResponseEntity.ok("Email khôi phục đã được gửi.");
    }

    // API đặt lại mật khẩu
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam("token") String token,
                                           @RequestParam("password") String password,
                                           @RequestParam("confirmPassword") String confirmPassword) {
        if (password.length() < 8) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Mật khẩu phải có ít nhất 8 ký tự.");
        }

        if (!password.equals(confirmPassword)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Mật khẩu không khớp.");
        }

        Optional<User> userOpt = userRepository.findByResetToken(token);
        if (!userOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token không hợp lệ.");
        }

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(password));
        user.setResetToken(null);
        userRepository.save(user);

        return ResponseEntity.ok("Đặt lại mật khẩu thành công.");
    }

    private String generateVerificationCode() {
        int randomCode = (int) (Math.random() * 900000) + 100000;
        return String.valueOf(randomCode);
    }
}
*/
