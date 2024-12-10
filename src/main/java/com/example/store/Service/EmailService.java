package com.example.store.Service;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationEmail(String toEmail, String verificationCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Xác minh tài khoản của bạn");
        message.setText("Vui lòng sử dụng mã này để xác minh tài khoản của bạn: " + verificationCode);
        mailSender.send(message);
    }

    public void sendPasswordChangeEmail(@NotBlank @Size(max = 100) @Email String email) {
    }

    public void sendResetPasswordEmail(@NotBlank @Size(max = 100) @Email String email, String resetUrl) {
    }
}
