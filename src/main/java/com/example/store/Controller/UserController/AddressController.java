package com.example.store.Controller.UserController;

import com.example.store.Entity.Address;
import com.example.store.Entity.AddressRequest;
import com.example.store.Service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@CrossOrigin("*")
public class AddressController {

    @Autowired
    private AddressService addressService;

    // Lấy danh sách địa chỉ của người dùng hiện tại
    @GetMapping
    public ResponseEntity<List<Address>> getUserAddresses() {
        List<Address> addresses = addressService.getAddressesByUser();
        return ResponseEntity.ok(addresses);
    }

    // Lấy địa chỉ mặc định của người dùng
    @GetMapping("/default")
    public ResponseEntity<Address> getDefaultAddress() {
        Address defaultAddress = addressService.getDefaultAddress();
        return ResponseEntity.ok(defaultAddress);
    }


    // Thêm địa chỉ mới
    @PostMapping
    public ResponseEntity<Address> addAddress(@RequestBody AddressRequest request) {
        Address newAddress = addressService.addAddress(request);
        return ResponseEntity.ok(newAddress);
    }

    // Cập nhật địa chỉ
    @PutMapping("/{id}")
    public ResponseEntity<Address> updateAddress(
            @PathVariable Long id,
            @RequestBody AddressRequest request) {
        Address updatedAddress = addressService.updateAddress(id, request);
        return ResponseEntity.ok(updatedAddress);
    }

    // Đặt địa chỉ mặc định
    @PutMapping("/{id}/set-default")
    public ResponseEntity<Address> setDefaultAddress(@PathVariable Long id) {
        Address defaultAddress = addressService.setDefaultAddress(id);
        return ResponseEntity.ok(defaultAddress);
    }

    // Xóa địa chỉ
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return ResponseEntity.noContent().build();
    }
}
