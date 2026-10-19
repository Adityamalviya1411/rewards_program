package com.charter.rewards.repository;

import com.charter.rewards.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Handles all the database reads and writes for customers.
 *
 * Spring Data gives us the standard CRUD methods for free.
 * We only need to add custom queries if the built-ins don't cover it.
 */
public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
