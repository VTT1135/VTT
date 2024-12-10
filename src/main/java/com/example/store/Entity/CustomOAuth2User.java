package com.example.store.Entity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User, UserDetails {
    private final User user;
    private final Map<String, Object> attributes;

    public CustomOAuth2User(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    @Override
    public String getName() {
        return user.getUsername();
    }

    // Implement các phương thức còn lại từ UserDetails
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null; // Thêm logic nếu bạn có quyền
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail(); // Hoặc một trường nào đó bạn muốn sử dụng
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true; // Thay đổi nếu có trạng thái kích hoạt
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
