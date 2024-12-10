package com.example.store.Entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressRequest {
    private Long userId;
    private String recipientName;
    private String phoneNumber;
    private String address;
}
