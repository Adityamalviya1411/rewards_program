package com.charter.rewards.service;

import com.charter.rewards.dto.CustomerRewardSummary;
import com.charter.rewards.exception.CustomerNotFoundException;
import com.charter.rewards.model.Customer;
import com.charter.rewards.model.Transaction;
import com.charter.rewards.repository.CustomerRepository;
import com.charter.rewards.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Unit tests for RewardsService.
 *
 * We mock the repositories so these tests run fast and don't need a database.
 * Covers the points calculation rules, monthly grouping, multi-customer
 * scenarios, and all the ways things can go wrong.
 */
@ExtendWith(MockitoExtension.class)
class RewardsServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private RewardsService rewardsService;

    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        startDate = LocalDate.of(2024, 1, 1);
        endDate = LocalDate.of(2024, 3, 31);
    }

    // --- points calculation ---------------------------------------------------

    @Test
    @DisplayName("$120 purchase earns 90 points (2x20 + 1x50)")
    void calculatePoints_120dollars_returns90() {
        assertThat(rewardsService.calculatePoints(new BigDecimal("120"))).isEqualTo(90);
    }

    @Test
    @DisplayName("$200 purchase earns 250 points (2x100 + 1x50)")
    void calculatePoints_200dollars_returns250() {
        assertThat(rewardsService.calculatePoints(new BigDecimal("200"))).isEqualTo(250);
    }

    @Test
    @DisplayName("$75 purchase earns 25 points (1x25)")
    void calculatePoints_75dollars_returns25() {
        assertThat(rewardsService.calculatePoints(new BigDecimal("75"))).isEqualTo(25);
    }

    @Test
    @DisplayName("$50 purchase earns 0 points — right at the threshold, not above it")
    void calculatePoints_50dollars_returns0() {
        assertThat(rewardsService.calculatePoints(new BigDecimal("50"))).isEqualTo(0);
    }

    @Test
    @DisplayName("$100 purchase earns 50 points (1x50)")
    void calculatePoints_100dollars_returns50() {
        assertThat(rewardsService.calculatePoints(new BigDecimal("100"))).isEqualTo(50);
    }

    @Test
    @DisplayName("$0 purchase earns nothing")
    void calculatePoints_zeroDollars_returns0() {
        assertThat(rewardsService.calculatePoints(BigDecimal.ZERO)).isEqualTo(0);
    }

    @Test
    @DisplayName("$30 purchase earns nothing — below the $50 threshold")
    void calculatePoints_belowTierOne_returns0() {
        assertThat(rewardsService.calculatePoints(new BigDecimal("30"))).isEqualTo(0);
    }

    @Test
    @DisplayName("$300 purchase earns 550 points (2x200 + 1x50)")
    void calculatePoints_300dollars_returns550() {
        assertThat(rewardsService.calculatePoints(new BigDecimal("300"))).isEqualTo(550);
    }

    @Test
    @DisplayName("$50.01 earns 0 points — fractional cents don't count toward the first tier")
    void calculatePoints_justAboveTierOne_returns0() {
        assertThat(rewardsService.calculatePoints(new BigDecimal("50.01"))).isEqualTo(0);
    }

    @Test
    @DisplayName("$100.01 earns 50 points — just crossed into the second tier")
    void calculatePoints_justAboveTierTwo_returns50() {
        assertThat(rewardsService.calculatePoints(new BigDecimal("100.01"))).isEqualTo(50);
    }

    // --- points calculation — things that should blow up ----------------------

    @Test
    @DisplayName("Passing null as the amount should throw")
    void calculatePoints_nullAmount_throwsException() {
        assertThatThrownBy(() -> rewardsService.calculatePoints(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("non-negative");
    }

    @Test
    @DisplayName("Negative amounts don't make sense and should throw")
    void calculatePoints_negativeAmount_throwsException() {
        assertThatThrownBy(() -> rewardsService.calculatePoints(new BigDecimal("-10")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("non-negative");
    }

    // --- single customer rewards ----------------------------------------------

    @Test
    @DisplayName("Customer with purchases spread across 3 months gets the right breakdown")
    void calculateRewardsForCustomer_multipleMonths_returnsCorrectSummary() {
        Customer customer = buildCustomer(1L, "Aditya Malviya");
        List<Transaction> transactions = List.of(
                buildTransaction(customer, new BigDecimal("120"), LocalDate.of(2024, 1, 5)),
                buildTransaction(customer, new BigDecimal("75"),  LocalDate.of(2024, 1, 18)),
                buildTransaction(customer, new BigDecimal("200"), LocalDate.of(2024, 2, 10)),
                buildTransaction(customer, new BigDecimal("110"), LocalDate.of(2024, 3, 8))
        );

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(transactionRepository.findByCustomerIdAndDateRange(1L, startDate, endDate))
                .thenReturn(transactions);

        CustomerRewardSummary summary = rewardsService.calculateRewardsForCustomer(1L, startDate, endDate);

        assertThat(summary.getCustomerId()).isEqualTo(1L);
        assertThat(summary.getPointsByMonth()).containsEntry("2024-01", 115L); // 90 + 25
        assertThat(summary.getPointsByMonth()).containsEntry("2024-02", 250L);
        assertThat(summary.getPointsByMonth()).containsEntry("2024-03", 70L);
        assertThat(summary.getTotalPoints()).isEqualTo(435L);
    }

    @Test
    @DisplayName("Big spender — high-value transactions rack up points fast")
    void calculateRewardsForCustomer_highValueTransactions_returnsCorrectPoints() {
        Customer customer = buildCustomer(3L, "Himanshu Sharma");
        List<Transaction> transactions = List.of(
                buildTransaction(customer, new BigDecimal("300"), LocalDate.of(2024, 1, 7)),
                buildTransaction(customer, new BigDecimal("250"), LocalDate.of(2024, 3, 29))
        );

        when(customerRepository.findById(3L)).thenReturn(Optional.of(customer));
        when(transactionRepository.findByCustomerIdAndDateRange(3L, startDate, endDate))
                .thenReturn(transactions);

        CustomerRewardSummary summary = rewardsService.calculateRewardsForCustomer(3L, startDate, endDate);

        assertThat(summary.getPointsByMonth()).containsEntry("2024-01", 550L);
        assertThat(summary.getPointsByMonth()).containsEntry("2024-03", 450L);
        assertThat(summary.getTotalPoints()).isEqualTo(1000L);
    }

    @Test
    @DisplayName("Customer who never spent over $50 ends up with zero points")
    void calculateRewardsForCustomer_allBelowThreshold_returnsZeroPoints() {
        Customer customer = buildCustomer(4L, "Preeyanshu Singh");
        List<Transaction> transactions = List.of(
                buildTransaction(customer, new BigDecimal("20"), LocalDate.of(2024, 1, 9)),
                buildTransaction(customer, new BigDecimal("50"), LocalDate.of(2024, 1, 23))
        );

        when(customerRepository.findById(4L)).thenReturn(Optional.of(customer));
        when(transactionRepository.findByCustomerIdAndDateRange(4L, startDate, endDate))
                .thenReturn(transactions);

        CustomerRewardSummary summary = rewardsService.calculateRewardsForCustomer(4L, startDate, endDate);

        assertThat(summary.getTotalPoints()).isEqualTo(0L);
    }

    @Test
    @DisplayName("Customer with no purchases in the range still gets a summary — just all zeros")
    void calculateRewardsForCustomer_noTransactions_returnsZeroPoints() {
        Customer customer = buildCustomer(2L, "Shreya Gupta");
        when(customerRepository.findById(2L)).thenReturn(Optional.of(customer));
        when(transactionRepository.findByCustomerIdAndDateRange(2L, startDate, endDate))
                .thenReturn(List.of());

        CustomerRewardSummary summary = rewardsService.calculateRewardsForCustomer(2L, startDate, endDate);

        assertThat(summary.getTotalPoints()).isEqualTo(0L);
        assertThat(summary.getPointsByMonth()).containsOnlyKeys("2024-01", "2024-02", "2024-03");
        assertThat(summary.getPointsByMonth().values()).containsOnly(0L);
    }

    @Test
    @DisplayName("Months with no purchases still show up in the response with 0 points")
    void calculateRewardsForCustomer_partialMonths_allMonthKeysPresent() {
        Customer customer = buildCustomer(1L, "Aditya Malviya");
        List<Transaction> transactions = List.of(
                buildTransaction(customer, new BigDecimal("120"), LocalDate.of(2024, 1, 5))
        );

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(transactionRepository.findByCustomerIdAndDateRange(1L, startDate, endDate))
                .thenReturn(transactions);

        CustomerRewardSummary summary = rewardsService.calculateRewardsForCustomer(1L, startDate, endDate);

        assertThat(summary.getPointsByMonth()).containsOnlyKeys("2024-01", "2024-02", "2024-03");
        assertThat(summary.getPointsByMonth()).containsEntry("2024-02", 0L);
        assertThat(summary.getPointsByMonth()).containsEntry("2024-03", 0L);
    }

    // --- single customer rewards — things that should blow up -----------------

    @Test
    @DisplayName("Looking up a customer ID that doesn't exist should throw CustomerNotFoundException")
    void calculateRewardsForCustomer_unknownId_throwsCustomerNotFoundException() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rewardsService.calculateRewardsForCustomer(99L, startDate, endDate))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("Start date after end date should throw — that range makes no sense")
    void calculateRewardsForCustomer_invalidDateRange_throwsException() {
        assertThatThrownBy(() -> rewardsService.calculateRewardsForCustomer(
                1L, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 1, 1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Null start date should throw")
    void calculateRewardsForCustomer_nullStartDate_throwsException() {
        assertThatThrownBy(() -> rewardsService.calculateRewardsForCustomer(1L, null, endDate))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Null end date should throw")
    void calculateRewardsForCustomer_nullEndDate_throwsException() {
        assertThatThrownBy(() -> rewardsService.calculateRewardsForCustomer(1L, startDate, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // --- all customers rewards ------------------------------------------------

    @Test
    @DisplayName("Two customers with transactions both get their own summary")
    void calculateRewardsForAll_multipleCustomers_returnsOneSummaryEach() {
        Customer aditya = buildCustomer(1L, "Aditya Malviya");
        Customer shreya = buildCustomer(2L, "Shreya Gupta");

        List<Transaction> transactions = List.of(
                buildTransaction(aditya, new BigDecimal("120"), LocalDate.of(2024, 1, 5)),
                buildTransaction(aditya, new BigDecimal("75"),  LocalDate.of(2024, 2, 10)),
                buildTransaction(shreya, new BigDecimal("150"), LocalDate.of(2024, 1, 19)),
                buildTransaction(shreya, new BigDecimal("80"),  LocalDate.of(2024, 3, 5))
        );

        when(transactionRepository.findAllByDateRange(startDate, endDate)).thenReturn(transactions);

        List<CustomerRewardSummary> summaries = rewardsService.calculateRewardsForAll(startDate, endDate);

        assertThat(summaries).hasSize(2);
    }

    @Test
    @DisplayName("Each customer's total is calculated independently — no cross-contamination")
    void calculateRewardsForAll_multipleCustomers_correctTotalsPerCustomer() {
        Customer aditya = buildCustomer(1L, "Aditya Malviya");
        Customer himanshu = buildCustomer(3L, "Himanshu Sharma");

        List<Transaction> transactions = List.of(
                buildTransaction(aditya,   new BigDecimal("120"), LocalDate.of(2024, 1, 5)),  // 90 pts
                buildTransaction(himanshu, new BigDecimal("300"), LocalDate.of(2024, 1, 7))   // 550 pts
        );

        when(transactionRepository.findAllByDateRange(startDate, endDate)).thenReturn(transactions);

        List<CustomerRewardSummary> summaries = rewardsService.calculateRewardsForAll(startDate, endDate);

        CustomerRewardSummary adityaSummary = summaries.stream()
                .filter(s -> s.getCustomerId().equals(1L)).findFirst().orElseThrow();
        CustomerRewardSummary himanshuSummary = summaries.stream()
                .filter(s -> s.getCustomerId().equals(3L)).findFirst().orElseThrow();

        assertThat(adityaSummary.getTotalPoints()).isEqualTo(90L);
        assertThat(himanshuSummary.getTotalPoints()).isEqualTo(550L);
    }

    @Test
    @DisplayName("No transactions in the range means an empty list comes back")
    void calculateRewardsForAll_noTransactions_returnsEmptyList() {
        when(transactionRepository.findAllByDateRange(startDate, endDate)).thenReturn(List.of());

        assertThat(rewardsService.calculateRewardsForAll(startDate, endDate)).isEmpty();
    }

    @Test
    @DisplayName("Null start date on the all-customers call should throw")
    void calculateRewardsForAll_nullStartDate_throwsException() {
        assertThatThrownBy(() -> rewardsService.calculateRewardsForAll(null, endDate))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Start after end on the all-customers call should throw")
    void calculateRewardsForAll_startAfterEnd_throwsException() {
        assertThatThrownBy(() -> rewardsService.calculateRewardsForAll(endDate, startDate))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // --- test helpers ---------------------------------------------------------

    /**
     * Creates a Customer with the given ID and name using reflection.
     * We have to do it this way because the entity doesn't have a public
     * all-args constructor — JPA owns that.
     *
     * @param id   the ID to assign
     * @param name the name to assign
     * @return a Customer ready to use in tests
     */
    private Customer buildCustomer(Long id, String name) {
        try {
            Customer customer = new Customer();
            setField(customer, "id", id);
            setField(customer, "name", name);
            setField(customer, "email", name.toLowerCase(Locale.ROOT).replace(" ", ".") + "@example.com");
            return customer;
        } catch (Exception e) {
            throw new RuntimeException("Failed to build Customer test fixture", e);
        }
    }

    /**
     * Creates a Transaction with the given details using reflection.
     * Same reason as buildCustomer — no public all-args constructor on the entity.
     *
     * @param customer who made the purchase
     * @param amount   how much they spent
     * @param date     when they spent it
     * @return a Transaction ready to use in tests
     */
    private Transaction buildTransaction(Customer customer, BigDecimal amount, LocalDate date) {
        try {
            Transaction transaction = new Transaction();
            setField(transaction, "customer", customer);
            setField(transaction, "amount", amount);
            setField(transaction, "transactionDate", date);
            return transaction;
        } catch (Exception e) {
            throw new RuntimeException("Failed to build Transaction test fixture", e);
        }
    }

    /**
     * Pokes a value into a private field using reflection.
     * Only used in tests — not something we'd do in production code.
     *
     * @param target    the object to modify
     * @param fieldName the field to set
     * @param value     the value to put in it
     * @throws Exception if the field doesn't exist or can't be accessed
     */
    private void setField(Object target, String fieldName, Object value) throws Exception {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
