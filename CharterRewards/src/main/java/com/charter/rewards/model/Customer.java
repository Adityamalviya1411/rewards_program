package com.charter.rewards.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;

/**
 * Represents a customer in the rewards program.
 *
 * Each customer can have any number of transactions, which are used
 * to figure out how many reward points they've earned.
 */
@Entity
@Table(name = "customers")
public class Customer {

    /** Database-generated ID — we never set this ourselves. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The customer's full name. */
    @Column(nullable = false)
    private String name;

    /** Email address — must be unique across all customers. */
    @Column(nullable = false, unique = true)
    private String email;

    /** All the purchases this customer has made. */
    @OneToMany(mappedBy = "customer")
    private List<Transaction> transactions;

    /**
     * @return the customer's database ID
     */
    public Long getId() {
        return id;
    }

    /**
     * @return the customer's full name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the customer's email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * @return all transactions linked to this customer
     */
    public List<Transaction> getTransactions() {
        return transactions;
    }
}
