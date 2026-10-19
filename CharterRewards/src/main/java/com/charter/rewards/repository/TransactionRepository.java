package com.charter.rewards.repository;

import com.charter.rewards.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

/**
 * Handles all the database reads for transactions.
 *
 * The two custom queries here are the main ones the service relies on —
 * one to grab all transactions across all customers for a date range,
 * and one to narrow it down to a single customer.
 *
 * We use JOIN FETCH on the customer so we don't end up with N+1 queries
 * when the service iterates over the results.
 */
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Fetches every transaction that falls within the given date range,
     * along with the customer who made it.
     *
     * @param startDate first day of the range (inclusive)
     * @param endDate   last day of the range (inclusive)
     * @return all matching transactions, each with their customer loaded
     */
    @Query("SELECT t FROM Transaction t JOIN FETCH t.customer WHERE t.transactionDate BETWEEN :startDate AND :endDate")
    List<Transaction> findAllByDateRange(@Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);

    /**
     * Fetches all transactions for one specific customer within the date range.
     *
     * @param customerId the customer we're interested in
     * @param startDate  first day of the range (inclusive)
     * @param endDate    last day of the range (inclusive)
     * @return the customer's transactions in that period
     */
    @Query("SELECT t FROM Transaction t JOIN FETCH t.customer WHERE t.customer.id = :customerId AND t.transactionDate BETWEEN :startDate AND :endDate")
    List<Transaction> findByCustomerIdAndDateRange(@Param("customerId") Long customerId,
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);
}
