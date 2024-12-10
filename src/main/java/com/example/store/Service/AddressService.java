package com.example.store.Service;

import com.example.store.Entity.Address;
import com.example.store.Entity.AddressRequest;
import com.example.store.Entity.User;
import com.example.store.Repository.AddressRepository;
import com.example.store.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    // Lấy userId của người dùng hiện tại
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            User user = (User) authentication.getPrincipal();
            return user.getId();
        }
        throw new RuntimeException("User is not authenticated");
    }

    // Lấy danh sách địa chỉ của người dùng
    public List<Address> getAddressesByUser() {
        Long userId = getCurrentUserId();
        return addressRepository.findByUserId(userId);
    }

    public Address getDefaultAddress() {
        Long userId = getCurrentUserId(); // Sử dụng phương thức lấy userId
        return addressRepository.findByUserIdAndIsDefault(userId, true)
                .orElse(null); // Trả về null nếu không có địa chỉ mặc định
    }


    // Thêm địa chỉ mới
    public Address addAddress(AddressRequest request) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Kiểm tra xem người dùng đã có địa chỉ mặc định chưa
        Optional<Address> existingDefaultAddress = addressRepository.findByUserIdAndIsDefault(userId, true);
        if (existingDefaultAddress.isPresent()) {
            // Nếu đã có địa chỉ mặc định, đặt địa chỉ đó thành không mặc định
            Address currentDefault = existingDefaultAddress.get();
            currentDefault.setIsDefault(false);
            addressRepository.save(currentDefault);
        }

        // Tạo địa chỉ mới và đặt làm mặc định
        Address address = new Address();
        address.setUser(user);
        address.setRecipientName(request.getRecipientName());
        address.setPhoneNumber(request.getPhoneNumber());
        address.setAddress(request.getAddress());
        address.setIsDefault(true); // Đặt địa chỉ mới là mặc định
        return addressRepository.save(address);
    }

    // Cập nhật địa chỉ
    public Address updateAddress(Long id, AddressRequest request) {
        Long userId = getCurrentUserId();
        Address address = addressRepository.findById(id).orElseThrow(() -> new RuntimeException("Address not found"));
        if (!address.getUser().getId().equals(userId)) {
            throw new RuntimeException("User not authorized to update this address");
        }
        address.setRecipientName(request.getRecipientName());
        address.setPhoneNumber(request.getPhoneNumber());
        address.setAddress(request.getAddress());
        return addressRepository.save(address);
    }

    // Đặt địa chỉ mặc định
    public Address setDefaultAddress(Long id) {
        Long userId = getCurrentUserId();
        addressRepository.removeDefaultAddress(userId);
        Address address = addressRepository.findById(id).orElseThrow(() -> new RuntimeException("Address not found"));
        if (!address.getUser().getId().equals(userId)) {
            throw new RuntimeException("User not authorized to set this address as default");
        }
        address.setIsDefault(true);
        return addressRepository.save(address);
    }

    // Xóa địa chỉ
    public void deleteAddress(Long id) {
        Long userId = getCurrentUserId();
        Address address = addressRepository.findById(id).orElseThrow(() -> new RuntimeException("Address not found"));
        if (!address.getUser().getId().equals(userId)) {
            throw new RuntimeException("User not authorized to delete this address");
        }
        addressRepository.deleteById(id);
    }
}
