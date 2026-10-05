package com.halcyon.hotel.repository;

import com.halcyon.hotel.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByAccountIdOrderByCreatedAtDesc(Long accountId);
    Optional<Payment> findByBookingId(Long bookingId);
    void deleteByAccountId(Long accountId);
}
