package com.halcyon.hotel.controller;

import com.halcyon.hotel.entity.Account;
import com.halcyon.hotel.entity.Payment;
import com.halcyon.hotel.entity.SavedCard;
import com.halcyon.hotel.repository.AccountRepository;
import com.halcyon.hotel.repository.PaymentRepository;
import com.halcyon.hotel.repository.SavedCardRepository;
import com.halcyon.hotel.security.RoleGuard;
import com.halcyon.hotel.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final SavedCardRepository savedCardRepository;
    private final AccountRepository accountRepository;

    @GetMapping
    public List<Payment> all(HttpServletRequest request) {
        RoleGuard.requireRole(request, "ADMIN");
        return paymentRepository.findAll();
    }

    @GetMapping("/mine")
    public List<Payment> mine(HttpServletRequest request) {
        Long accountId = RoleGuard.requireAuth(request);
        return paymentRepository.findByAccountIdOrderByCreatedAtDesc(accountId);
    }

    /**
     * Pay an invoice. Body shapes:
     *   { "method": "CASH" }
     *   { "method": "CARD", "cardId": 5 }                                      // pay with a saved card
     *   { "method": "CARD", "cardholderName": "...", "cardNumber": "...",      // pay with a new card
     *     "expiryMonth": 12, "expiryYear": 2029, "cvv": "123", "saveCard": true }
     * The CVV (if supplied) is only used to look real for the demo — it is never persisted anywhere.
     */
    @PostMapping("/{id}/pay")
    public Payment pay(HttpServletRequest request, @PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long accountId = RoleGuard.requireAuth(request);
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role) && !payment.getAccount().getId().equals(accountId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "That isn't your invoice");
        }

        Payment.Method method = parseMethod(String.valueOf(body.get("method")));
        String cardBrand = null;
        String cardLast4 = null;

        if (method == Payment.Method.CARD) {
            if (body.get("cardId") != null) {
                Long cardId = Long.valueOf(String.valueOf(body.get("cardId")));
                SavedCard card = savedCardRepository.findById(cardId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Saved card not found"));
                if (!card.getAccount().getId().equals(accountId)) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "That isn't your card");
                }
                cardBrand = card.getBrand();
                cardLast4 = card.getLast4();
            } else {
                Account account = accountRepository.findById(accountId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
                SavedCard entered = CardController.buildCard(account, body);
                cardBrand = entered.getBrand();
                cardLast4 = entered.getLast4();
                if (Boolean.TRUE.equals(body.get("saveCard"))) {
                    savedCardRepository.save(entered);
                }
            }
        }

        return paymentService.pay(id, method, cardBrand, cardLast4);
    }

    @PostMapping("/{id}/refund")
    public Payment refund(HttpServletRequest request, @PathVariable Long id) {
        RoleGuard.requireRole(request, "ADMIN");
        return paymentService.refund(id);
    }

    @PostMapping
    public Payment createManual(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        RoleGuard.requireRole(request, "ADMIN");
        Long accountId = Long.valueOf(String.valueOf(body.get("accountId")));
        BigDecimal amount = new BigDecimal(String.valueOf(body.get("amount")));
        Payment.Method method = parseMethod(String.valueOf(body.get("method")));
        Payment.Status status = Payment.Status.valueOf(String.valueOf(body.getOrDefault("status", "PAID")));
        return paymentService.createManualCharge(accountId, amount, method, status);
    }

    @PutMapping("/{id}")
    public Payment update(HttpServletRequest request, @PathVariable Long id, @RequestBody Payment payload) {
        RoleGuard.requireRole(request, "ADMIN");
        Payment existing = paymentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));
        existing.setAmount(payload.getAmount());
        existing.setMethod(payload.getMethod());
        existing.setStatus(payload.getStatus());
        return paymentRepository.save(existing);
    }

    private Payment.Method parseMethod(String raw) {
        try {
            return Payment.Method.valueOf(raw);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment method must be CARD or CASH");
        }
    }
}
