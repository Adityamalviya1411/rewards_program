package com.charter.rewards.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents a single purchase made by a customer.
 *
 * The amount and date are what the rewards calculation actually cares about.
 * The customer link tells us who the purchase belongs to.
 */
@Entity
@Table(name = "transactions")
public class Transaction {

    /** Database-generated ID — we never set this ourselves. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The customer who made this purchase. Loaded lazily to keep things efficient. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /** How much the customer spent. Can't be negative. */
    @Column(nullable = false)
    private BigDecimal amount;

    /** The day the purchase happened. */
    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    /**
     * @return the transaction's database ID
     */
    public Long getId() {
        return id;
    }

    /**
     * @return the customer who made this purchase
     */
    public Customer getCustomer() {
        return customer;
    }

    /**
     * @return the dollar amount spent
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * @return the date the purchase was made
     */
    public LocalDate getTransactionDate() {
        return transactionDate;
    }
}
