package com.halcyon.hotel.controller;

import com.halcyon.hotel.entity.Account;
import com.halcyon.hotel.entity.SavedCard;
import com.halcyon.hotel.repository.AccountRepository;
import com.halcyon.hotel.repository.SavedCardRepository;
import com.halcyon.hotel.security.RoleGuard;
import com.halcyon.hotel.util.CardUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final SavedCardRepository savedCardRepository;
    private final AccountRepository accountRepository;

    @GetMapping("/mine")
    public List<SavedCard> mine(HttpServletRequest request) {
        Long accountId = RoleGuard.requireAuth(request);
        return savedCardRepository.findByAccountIdOrderByCreatedAtDesc(accountId);
    }

    @PostMapping
    public SavedCard add(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        Long accountId = RoleGuard.requireAuth(request);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

        SavedCard card = buildCard(account, body);
        return savedCardRepository.save(card);
    }

    @DeleteMapping("/{id}")
    public void delete(HttpServletRequest request, @PathVariable Long id) {
        Long accountId = RoleGuard.requireAuth(request);
        SavedCard card = savedCardRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found"));
        if (!card.getAccount().getId().equals(accountId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "That isn't your card");
        }
        savedCardRepository.deleteById(id);
    }

    /** Shared by /api/cards (save for later) and PaymentController (one-off card entry). */
    public static SavedCard buildCard(Account account, Map<String, Object> body) {
        String cardholderName = String.valueOf(body.get("cardholderName"));
        String digits = CardUtils.stripSpaces(String.valueOf(body.get("cardNumber")));
        int month;
        int year;
        try {
            month = Integer.parseInt(String.valueOf(body.get("expiryMonth")));
            year = Integer.parseInt(String.valueOf(body.get("expiryYear")));
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid expiry date");
        }

        if (cardholderName == null || cardholderName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cardholder name is required");
        }
        if (!CardUtils.isValidCardNumber(digits)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "That card number doesn't look valid");
        }
        YearMonth expiry = YearMonth.of(year, month);
        if (expiry.isBefore(YearMonth.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "That card has expired");
        }

        SavedCard card = new SavedCard();
        card.setAccount(account);
        card.setCardholderName(cardholderName.trim());
        card.setCardNumber(digits);
        card.setBrand(CardUtils.detectBrand(digits));
        card.setLast4(CardUtils.last4(digits));
        card.setExpiryMonth(month);
        card.setExpiryYear(year);
        return card;
    }
}
