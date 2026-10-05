package com.halcyon.hotel.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * NOTE (demo/coursework project — not production-grade payment handling):
 * Real payment systems never store a raw card number like this; they tokenize
 * through a PCI-compliant processor (Stripe, Braintree, etc.) and only keep a
 * token + last4 + brand. This entity stores the number directly purely so the
 * assignment's "save card details" requirement has something concrete to show;
 * it is marked write-only so the API never sends it back out, and the CVV is
 * never persisted at all (that one's a hard rule even in a demo).
 */
@Entity
@Table(name = "cards")
@Data
@NoArgsConstructor
public class SavedCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "cardholder_name", nullable = false, length = 150)
    private String cardholderName;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "card_number", nullable = false, length = 25)
    private String cardNumber;

    @Column(nullable = false, length = 30)
    private String brand;

    @Column(nullable = false, length = 4)
    private String last4;

    @Column(name = "expiry_month", nullable = false)
    private Integer expiryMonth;

    @Column(name = "expiry_year", nullable = false)
    private Integer expiryYear;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
