package com.example.store.Controller;

import com.example.store.Entity.Role;
import com.example.store.Entity.User;
import com.example.store.Repository.RoleRepository;
import com.example.store.Repository.UserRepository;
import com.example.store.Service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@Controller
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EmailService emailService;


    @GetMapping("/profile")
    public String getUserProfile(Model model) {
        // Lấy Authentication từ SecurityContextHolder
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Kiểm tra xem người dùng đã đăng nhập chưa
        if (authentication != null && authentication.isAuthenticated()) {
            User user = (User) authentication.getPrincipal();
            Long userId = user.getId();

            // Lấy thông tin người dùng từ cơ sở dữ liệu
            Optional<User> userOpt = userRepository.findById(userId);

            if (userOpt.isPresent()) {
                User foundUser = userOpt.get();
                model.addAttribute("user", foundUser);
                return "views/profile";
            }
        }
        return "redirect:/login";
    }

    @PostMapping("/profile")
    public String updateUserProfile(@ModelAttribute("user") User user,
                                    BindingResult result,
                                    Model model) {

        if (result.hasErrors()) {
            return "views/profile";
        }

        // Lấy Authentication từ SecurityContextHolder
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            User currentUser = (User) authentication.getPrincipal();
            Long userId = currentUser.getId();

            // Tìm người dùng trong cơ sở dữ liệu
            Optional<User> userOpt = userRepository.findById(userId);

            if (userOpt.isPresent()) {
                User existingUser = userOpt.get();

                // Chỉ cập nhật các trường nếu chúng có thay đổi
                if (user.getUsername() != null && !user.getUsername().isEmpty()) {
                    existingUser.setUsername(user.getUsername());
                }

                if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                    if (!user.getPassword().equals(user.getConfirmPassword())) {
                        model.addAttribute("error", "Mật khẩu không khớp.");
                        model.addAttribute("user", existingUser); // Trả lại thông tin người dùng gốc
                        return "views/profile";
                    }
                    existingUser.setPassword(passwordEncoder.encode(user.getPassword()));

                    // Gửi email thông báo đổi mật khẩu
                    String content = "Mật khẩu của bạn đã được thay đổi.";
                    SimpleMailMessage email = new SimpleMailMessage();
                    email.setTo(existingUser.getEmail());
                    email.setSubject("Đổi mật khẩu");
                    email.setText(content);
                    mailSender.send(email);
                }

                // Lưu thông tin người dùng sau khi cập nhật
                userRepository.save(existingUser);

                model.addAttribute("message", "Thông tin đã được cập nhật thành công.");
                model.addAttribute("user", existingUser); // Trả lại thông tin người dùng gốc
                return "views/profile";
            } else {
                model.addAttribute("error", "Không tìm thấy người dùng.");
                return "views/profile";
            }
        }
        return "redirect:/login";
    }



    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") @Valid User user, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "register";
        }

        if (!user.getPassword().equals(user.getConfirmPassword())) {
            model.addAttribute("errorMessage", "Mật khẩu không trùng khớp.");
            return "register";
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            model.addAttribute("emailError", "Email đã được sử dụng.");
            return "register";
        }

        // Mã hóa mật khẩu và lưu người dùng
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEmailVerified(false); // Đặt mặc định là chưa xác minh email
        String verificationCode = generateVerificationCode();
        user.setVerificationCode(verificationCode); // Lưu mã xác minh trong database

        // Kiểm tra nếu đây là người dùng đầu tiên đăng ký
        boolean isFirstUser = userRepository.count() == 0;

        // Lấy vai trò USER từ cơ sở dữ liệu
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy vai trò USER."));
        user.getRoles().add(userRole);

        if (isFirstUser) {
            // Nếu đây là người dùng đầu tiên, thêm vai trò ADMIN
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy vai trò ADMIN."));
            user.getRoles().add(adminRole);
        }

        userRepository.save(user);

        // Gửi mã xác minh qua email
        emailService.sendVerificationEmail(user.getEmail(), verificationCode);

        return "redirect:/verify-email";
    }


    private String generateVerificationCode() {
        /*return UUID.randomUUID().toString(); // Tạo mã xác minh duy nhất*/
        int randomCode = (int) (Math.random() * 900000) + 100000;
        return String.valueOf(randomCode);
    }

    @GetMapping("/verify-email")
    public String verifyEmail() {
        return "verify-email";
    }

    @PostMapping("/verify-email")
    public String verifyEmail(@RequestParam("verificationCode") String code, Model model) {
        Optional<User> userOptional = userRepository.findByVerificationCode(code);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setEmailVerified(true);
            user.setVerificationCode(null);
            userRepository.save(user);
            return "redirect:/login?verified=true";
        } else {
            model.addAttribute("error", "Mã xác minh không hợp lệ hoặc đã hết hạn.");
            return "verify-email";
        }
    }


    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password, HttpSession session, Model model) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = (User) authentication.getPrincipal();

            // Lưu userId vào session
            session.setAttribute("userId", user.getId());
            System.out.println("User ID saved in session: " + user.getId());

            return "redirect:/index";
        } catch (BadCredentialsException e) {
            model.addAttribute("error", "Email hoặc mật khẩu không đúng");
            return "login";
        } catch (ClassCastException e) {
            model.addAttribute("error", "Có lỗi xảy ra khi xác thực người dùng");
            return "/verify-email";
        }

    }


    @GetMapping("/login")
    public String showLoginForm(Model model, @RequestParam(value = "error", required = false) String error) {
        if (error != null) {
            model.addAttribute("errorMessage", "Tài khoản hoặc mật khẩu không đúng.");
        }
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }


    // 1. Hiển thị form yêu cầu đặt lại mật khẩu
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(Model model) {
        model.addAttribute("email", "");
        return "forgot-password";
    }

    // 2. Xử lý yêu cầu gửi email đặt lại mật khẩu
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, Model model) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (!userOpt.isPresent()) {
            model.addAttribute("error", "Email không tồn tại");
            return "forgot-password";
        }

        User user = userOpt.get();

        // Tạo mã thông báo đặt lại mật khẩu (reset token)
        String token = UUID.randomUUID().toString();

        // Lưu token vào cơ sở dữ liệu
        user.setResetToken(token);
        userRepository.save(user);

        // Gửi email đặt lại mật khẩu
        String resetUrl = "http://localhost:8080/reset-password?token=" + token;
        try {
            sendResetPasswordEmail(user.getEmail(), resetUrl);
        } catch (MessagingException e) {
            model.addAttribute("error", "Có lỗi xảy ra khi gửi email.");
            return "forgot-password";
        }

        model.addAttribute("message", "Email khôi phục đã được gửi.");
        return "forgot-password";
    }


    // 3. Phương thức gửi email khôi phục mật khẩu
    public void sendResetPasswordEmail(String recipientEmail, String resetUrl) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setTo(recipientEmail);
        helper.setSubject("Đặt lại mật khẩu");

        String content = "<p>Xin chào,</p>"
                + "<p>Bạn đã yêu cầu đặt lại mật khẩu. Vui lòng nhấn vào liên kết dưới đây để đặt lại mật khẩu:</p>"
                + "<p><a href=\"" + resetUrl + "\">Đặt lại mật khẩu</a></p>"
                + "<br>"
                + "<p>Nếu bạn không yêu cầu điều này, vui lòng bỏ qua email này.</p>";

        helper.setText(content, true);
        mailSender.send(mimeMessage);
    }

    // 4. Hiển thị form đặt lại mật khẩu
    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        Optional<User> userOpt = userRepository.findByResetToken(token);
        if (!userOpt.isPresent()) {
            model.addAttribute("error", "Token không hợp lệ hoặc đã hết hạn.");
            return "reset-password"; // Tạo trang reset-password.html với thông báo lỗi
        }

        model.addAttribute("token", token);
        return "reset-password"; // Tạo trang reset-password.html với form đặt lại mật khẩu
    }

    // 5. Xử lý đặt lại mật khẩu mới
    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam("token") String token,
                                @RequestParam("password") String password,
                                @RequestParam("confirmPassword") String confirmPassword,
                                Model model) {
        if (password.length() < 8) {
            model.addAttribute("error", "Mật khẩu phải có ít nhất 8 ký tự.");
            model.addAttribute("token", token);
            return "reset-password";
        }
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu không khớp.");
            model.addAttribute("token", token);
            return "reset-password";
        }

        Optional<User> userOpt = userRepository.findByResetToken(token);
        if (!userOpt.isPresent()) {
            model.addAttribute("error", "Token không hợp lệ.");
            return "reset-password";
        }

        User user = userOpt.get();

        // Mã hóa mật khẩu mới và lưu vào cơ sở dữ liệu
        user.setPassword(passwordEncoder.encode(password));
        user.setResetToken(null); // Xóa token sau khi đã sử dụng
        userRepository.save(user);

        return "redirect:/login?resetSuccess";
    }
}
