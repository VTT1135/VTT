package com.example.store.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final CustomOAuth2UserService customOAuth2UserService; // Khai báo biến cho CustomOAuth2UserService


    public SecurityConfig(UserDetailsServiceImpl userDetailsService, CustomOAuth2UserService customOAuth2UserService) {
        this.userDetailsService = userDetailsService;
        this.customOAuth2UserService = customOAuth2UserService; // Gán giá trị cho biến
    }


    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorizeHttpRequests ->
                        authorizeHttpRequests
                                .requestMatchers("/api/auth/**","/login", "/register","/forgot-password","/reset-password","/verify-email", "/", "/index","/api/comments/**", "/views/home.html","/views/about.html","/views/news.html","/views/contact.html", "/views/shop.html", "/views/product-details.html", "/product/**", "/api/products/**", "/css/**", "/js/**", "/imageUrl/**")
                                .permitAll()
                                .requestMatchers("/AdminHome","/Admin-views/Admin-Statistics.html", "Admin-views/AdminProduct.html", "Admin-views/AdminCategory.html","/Admin-views/Admin-Promotions.html","/Admin-views/Admin-Orders.html","/admin/api/statistics/**","/admin/api/categories/**","/admin/api/products/**","/admin/api/promotions/**","/admin/api/orders/**").hasAnyRole("ADMIN", "STAFF")
                                .requestMatchers("/Admin-views/Admin-Users.html","/api/users/**")
                                .hasRole("ADMIN")
                                .anyRequest().authenticated()
                )
                .formLogin(formLogin ->
                        formLogin
                                .loginPage("/login")
                                .defaultSuccessUrl("/index", true)
                                .failureUrl("/login?error=true")
                                .usernameParameter("email")
                                .passwordParameter("password")
                                .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfoEndpoint ->
                                userInfoEndpoint.userService(customOAuth2UserService)) // Đăng ký CustomOAuth2UserService
                        .defaultSuccessUrl("/index", true)
                        .failureUrl("/login?error=true")
                )

                .logout(logout ->
                        logout
                                .logoutUrl("/logout")
                                .permitAll()
                )
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

