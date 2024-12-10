package com.example.store.Config;


import com.example.store.Entity.Role;
import com.example.store.Entity.User;
import com.example.store.Repository.RoleRepository;
import com.example.store.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    // Nếu không tồn tại, tạo người dùng mới
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setUsername(name);
                    newUser.setEmailVerified(true);

                    // Tạo mật khẩu ngẫu nhiên
                    String randomPassword = generateRandomPassword();
                    newUser.setPassword(new BCryptPasswordEncoder().encode(randomPassword));
                    // Tạo mã reset mật khẩu
                    String resetToken = UUID.randomUUID().toString();
                    newUser.setResetToken(resetToken);

                    // Kiểm tra xem đây có phải là người dùng đầu tiên không
                    Set<Role> roles = new HashSet<>();
                    if (userRepository.count() == 0) {
                        // Nếu là người dùng đầu tiên, gán vai trò ADMIN
                        roles.add(roleRepository.findByName("ROLE_ADMIN")
                                .orElseThrow(() -> new RuntimeException("Lỗi: Vai trò ADMIN không tồn tại.")));
                    } else {
                        // Các người dùng tiếp theo chỉ có vai trò USER
                        roles.add(roleRepository.findByName("ROLE_USER")
                                .orElseThrow(() -> new RuntimeException("Lỗi: Vai trò USER không tồn tại.")));
                    }
                    newUser.setRoles(roles);
// Gửi email với liên kết reset mật khẩu
                    sendPasswordResetEmail(newUser);
                    return userRepository.save(newUser);
                });
// Sau khi người dùng đã được tạo hoặc tìm thấy, đăng nhập người dùng vào hệ thống
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return user;
    }

    private String generateRandomPassword() {
        int length = 10;
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
    private void sendPasswordResetEmail(User user) {
        String resetLink = "http://localhost:8080/reset-password?token=" + user.getResetToken();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Cài đặt mật khẩu mới");
        message.setText("Chào " + user.getUsername() + ",\n\nVui lòng nhấp vào liên kết sau để tạo mật khẩu mới: " + resetLink);

        mailSender.send(message);
    }
}
