package com.charter.rewards.service;

import com.charter.rewards.dto.CustomerRewardSummary;
import com.charter.rewards.exception.CustomerNotFoundException;
import com.charter.rewards.model.Customer;
import com.charter.rewards.model.Transaction;
import com.charter.rewards.repository.CustomerRepository;
import com.charter.rewards.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The heart of the rewards program — this is where points get calculated.
 *
 * The rules are straightforward:
 *   - Spend $50 or less and you get nothing.
 *   - Spend between $50 and $100 and you earn 1 point per dollar above $50.
 *   - Spend over $100 and you earn 2 points per dollar above $100, plus
 *     the 50 points you already earned from the $50–$100 band.
 *
 * So a $120 purchase works out to: 2 x $20 + 1 x $50 = 90 points.
 */
@Service
public class RewardsService {

    private static final BigDecimal TIER_ONE_THRESHOLD = new BigDecimal("50");
    private static final BigDecimal TIER_TWO_THRESHOLD = new BigDecimal("100");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM", Locale.ROOT);

    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Spring wires the repositories in through the constructor.
     *
     * @param customerRepository    used to look up customers by ID
     * @param transactionRepository used to fetch transactions for a date range
     */
    public RewardsService(CustomerRepository customerRepository,
                          TransactionRepository transactionRepository) {
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Pulls all transactions in the date range and groups them by customer,
     * then builds a reward summary for each one.
     *
     * Customers who had no transactions in the range won't appear in the result.
     *
     * @param startDate beginning of the period to look at (inclusive)
     * @param endDate   end of the period (inclusive)
     * @return one summary per customer who had transactions in the range
     * @throws IllegalArgumentException if either date is null or start is after end
     */
    public List<CustomerRewardSummary> calculateRewardsForAll(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        List<Transaction> transactions = transactionRepository.findAllByDateRange(startDate, endDate);

        return transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getCustomer))
                .entrySet().stream()
                .map(entry -> buildSummary(entry.getKey(), entry.getValue(), startDate, endDate))
                .collect(Collectors.toList());
    }

    /**
     * Calculates the reward points for a single customer over the given date range.
     *
     * If the customer doesn't exist in the database, a 404 will come back.
     * If they exist but had no transactions in the range, you'll still get
     * a valid summary — just with zeros across all months.
     *
     * @param customerId the customer to look up
     * @param startDate  beginning of the period (inclusive)
     * @param endDate    end of the period (inclusive)
     * @return the customer's reward summary with per-month and total points
     * @throws CustomerNotFoundException if no customer exists with that ID
     * @throws IllegalArgumentException  if either date is null or start is after end
     */
    public CustomerRewardSummary calculateRewardsForCustomer(Long customerId,
                                                              LocalDate startDate,
                                                              LocalDate endDate) {
        validateDateRange(startDate, endDate);
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        List<Transaction> transactions = transactionRepository
                .findByCustomerIdAndDateRange(customerId, startDate, endDate);

        return buildSummary(customer, transactions, startDate, endDate);
    }

    /**
     * Works out how many reward points a single purchase amount is worth.
     *
     * This is the core business rule:
     *   - $0–$50      → 0 points
     *   - $50–$100    → 1 point per dollar above $50
     *   - Over $100   → 50 points + 2 points per dollar above $100
     *
     * @param amount the purchase amount — must be a positive number
     * @return the points earned, always 0 or more
     * @throws IllegalArgumentException if the amount is null or negative
     */
    public long calculatePoints(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Transaction amount must be non-negative");
        }
        long points = 0;
        if (amount.compareTo(TIER_TWO_THRESHOLD) > 0) {
            points += amount.subtract(TIER_TWO_THRESHOLD).longValue() * 2;
            points += TIER_TWO_THRESHOLD.subtract(TIER_ONE_THRESHOLD).longValue();
        } else if (amount.compareTo(TIER_ONE_THRESHOLD) > 0) {
            points += amount.subtract(TIER_ONE_THRESHOLD).longValue();
        }
        return points;
    }

    /**
     * Puts together the full reward summary for a customer — monthly breakdown
     * and the grand total.
     *
     * @param customer     the customer we're summarising
     * @param transactions their transactions within the date range
     * @param startDate    start of the period
     * @param endDate      end of the period
     * @return the completed summary ready to send back in the response
     */
    private CustomerRewardSummary buildSummary(Customer customer, List<Transaction> transactions,
                                                LocalDate startDate, LocalDate endDate) {
        Map<String, Long> pointsByMonth = buildMonthlyPointsMap(transactions, startDate, endDate);
        long totalPoints = pointsByMonth.values().stream().mapToLong(Long::longValue).sum();
        return new CustomerRewardSummary(customer.getId(), customer.getName(), pointsByMonth, totalPoints);
    }

    /**
     * Builds the month-by-month points breakdown for a set of transactions.
     *
     * Every month between startDate and endDate gets an entry — even months
     * where the customer didn't spend anything. That way the caller always
     * gets a complete picture of the period without any gaps.
     *
     * Months are derived dynamically from the date range, so nothing is
     * hard-coded here.
     *
     * @param transactions the transactions to tally up
     * @param startDate    determines which month to start from
     * @param endDate      determines which month to end at
     * @return an ordered map of "yyyy-MM" keys to points earned that month
     */
    private Map<String, Long> buildMonthlyPointsMap(List<Transaction> transactions,
                                                      LocalDate startDate, LocalDate endDate) {
        Map<String, Long> pointsByMonth = new LinkedHashMap<>();

        YearMonth current = YearMonth.from(startDate);
        YearMonth last = YearMonth.from(endDate);
        while (!current.isAfter(last)) {
            pointsByMonth.put(current.format(MONTH_FORMATTER), 0L);
            current = current.plusMonths(1);
        }

        for (Transaction transaction : transactions) {
            String monthKey = YearMonth.from(transaction.getTransactionDate()).format(MONTH_FORMATTER);
            long points = calculatePoints(transaction.getAmount());
            pointsByMonth.merge(monthKey, points, Long::sum);
        }
        return pointsByMonth;
    }

    /**
     * Quick sanity check on the date range before we do anything with it.
     *
     * Throws early with a clear message rather than letting something weird
     * happen downstream.
     *
     * @param startDate the start date to check
     * @param endDate   the end date to check
     * @throws IllegalArgumentException if either date is null, or if start comes after end
     */
    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date must not be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must not be after end date");
        }
    }
}
