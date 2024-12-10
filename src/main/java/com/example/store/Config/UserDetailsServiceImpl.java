package com.example.store.Config;

import com.example.store.Entity.User;
import com.example.store.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("Trying to load user with email: " + email); // Ghi log email được truyền vào

        Optional<User> userOptional = userRepository.findByEmail(email);  // Tìm kiếm theo email

        User user = userOptional.orElseThrow(() ->
                new UsernameNotFoundException("User not found with email: " + email));  // Thông báo lỗi nếu không tìm thấy

        System.out.println("User found: " + user); // Ghi log người dùng tìm thấy
        return user;
    }
}

