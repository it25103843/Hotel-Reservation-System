package com.halcyon.hotel.service;

import com.halcyon.hotel.entity.Account;
import com.halcyon.hotel.entity.Payment;
import com.halcyon.hotel.repository.AccountRepository;
import com.halcyon.hotel.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public Payment pay(Long paymentId, Payment.Method method, String cardBrand, String cardLast4) {
        Payment payment = get(paymentId);
        if (payment.getStatus() != Payment.Status.UNPAID) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Invoice is not payable");
        }
        payment.setMethod(method);
        payment.setStatus(Payment.Status.PAID);
        payment.setPaidAt(LocalDateTime.now());
        payment.setCardBrand(method == Payment.Method.CARD ? cardBrand : null);
        payment.setCardLast4(method == Payment.Method.CARD ? cardLast4 : null);
        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment refund(Long paymentId) {
        Payment payment = get(paymentId);
        if (payment.getStatus() != Payment.Status.PAID) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only paid invoices can be refunded");
        }
        payment.setStatus(Payment.Status.REFUNDED);
        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment createManualCharge(Long accountId, java.math.BigDecimal amount, Payment.Method method, Payment.Status status) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        Payment payment = new Payment();
        payment.setAccount(account);
        payment.setAmount(amount);
        payment.setMethod(method);
        payment.setStatus(status);
        if (status == Payment.Status.PAID) payment.setPaidAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    private Payment get(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
    }
}
