package com.example.store.Repository;

import com.example.store.Entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {

    // Sửa phương thức truy vấn
    List<Address> findByUserId(Long userId);
    Optional<Address> findByUserIdAndIsDefault(Long userId, boolean isDefault);
    @Modifying
    @Transactional
    @Query("UPDATE Address a SET a.isDefault = FALSE WHERE a.user.id = :userId")
    void removeDefaultAddress(Long userId);

}
