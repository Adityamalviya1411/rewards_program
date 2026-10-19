package com.charter.rewards.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@link RewardsController} using an H2 in-memory database
 * pre-loaded with the test SQL scripts (4 customers, 3 months of transactions).
 *
 * <p>These tests verify the full request-response cycle including JSON serialisation,
 * HTTP status codes, and correct reward point calculations end-to-end.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RewardsControllerIntegrationTest {

    private static final String BASE_URL = "/api/rewards";
    private static final String START    = "2024-01-01";
    private static final String END      = "2024-03-31";

    @Autowired
    private MockMvc mockMvc;

    // ── GET /api/rewards ─────────────────────────────────────────────────────

    /**
     * Verifies that all 4 customers are returned when querying the full 3-month range.
     */
    @Test
    @DisplayName("GET /api/rewards returns summaries for all 4 customers")
    void getAllRewards_validRange_returns4Summaries() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("startDate", START)
                        .param("endDate", END)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(4)));
    }

    /**
     * Verifies that each customer summary contains all 3 monthly keys for a 3-month range.
     */
    @Test
    @DisplayName("GET /api/rewards returns 3 monthly keys per customer")
    void getAllRewards_validRange_eachSummaryHas3MonthKeys() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("startDate", START)
                        .param("endDate", END))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].pointsByMonth['2024-01']").exists())
                .andExpect(jsonPath("$[0].pointsByMonth['2024-02']").exists())
                .andExpect(jsonPath("$[0].pointsByMonth['2024-03']").exists());
    }

    /**
     * Verifies that a single-month range produces exactly one monthly key in the response.
     */
    @Test
    @DisplayName("GET /api/rewards with single-month range returns 1 monthly key")
    void getAllRewards_singleMonth_returns1MonthKey() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].pointsByMonth['2024-01']").exists())
                .andExpect(jsonPath("$[0].pointsByMonth['2024-02']").doesNotExist());
    }

    /**
     * Verifies that a date range with no transactions returns an empty list.
     */
    @Test
    @DisplayName("GET /api/rewards with range outside data returns empty list")
    void getAllRewards_rangeOutsideData_returnsEmptyList() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("startDate", "2023-01-01")
                        .param("endDate", "2023-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    /**
     * Verifies that omitting startDate returns HTTP 400.
     */
    @Test
    @DisplayName("GET /api/rewards with missing startDate returns 400")
    void getAllRewards_missingStartDate_returns400() throws Exception {
        mockMvc.perform(get(BASE_URL).param("endDate", END))
                .andExpect(status().isBadRequest());
    }

    /**
     * Verifies that omitting endDate returns HTTP 400.
     */
    @Test
    @DisplayName("GET /api/rewards with missing endDate returns 400")
    void getAllRewards_missingEndDate_returns400() throws Exception {
        mockMvc.perform(get(BASE_URL).param("startDate", START))
                .andExpect(status().isBadRequest());
    }

    /**
     * Verifies that a non-ISO date format returns HTTP 400.
     */
    @Test
    @DisplayName("GET /api/rewards with invalid date format returns 400")
    void getAllRewards_invalidDateFormat_returns400() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("startDate", "01-01-2024")
                        .param("endDate", END))
                .andExpect(status().isBadRequest());
    }

    /**
     * Verifies that startDate after endDate returns HTTP 400.
     */
    @Test
    @DisplayName("GET /api/rewards with startDate after endDate returns 400")
    void getAllRewards_startAfterEnd_returns400() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("startDate", END)
                        .param("endDate", START))
                .andExpect(status().isBadRequest());
    }

    // ── GET /api/rewards/{customerId} ────────────────────────────────────────

    /**
     * Verifies Aditya's monthly and total points across the full 3-month period.
     * Expected: Jan=115, Feb=255, Mar=110, Total=480.
     */
    @Test
    @DisplayName("GET /api/rewards/1 returns Aditya's correct monthly and total points")
    void getRewardsForCustomer_aditya_returnsCorrectTotal() throws Exception {
        mockMvc.perform(get(BASE_URL + "/1")
                        .param("startDate", START)
                        .param("endDate", END))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName", is("Aditya Malviya")))
                .andExpect(jsonPath("$.pointsByMonth['2024-01']", is(115)))
                .andExpect(jsonPath("$.pointsByMonth['2024-02']", is(255)))
                .andExpect(jsonPath("$.pointsByMonth['2024-03']", is(110)))
                .andExpect(jsonPath("$.totalPoints", is(480)));
    }

    /**
     * Verifies Shreya's monthly and total points across the full 3-month period.
     * Expected: Jan=160, Feb=140, Mar=200, Total=500.
     */
    @Test
    @DisplayName("GET /api/rewards/2 returns Shreya's correct monthly and total points")
    void getRewardsForCustomer_shreya_returnsCorrectTotal() throws Exception {
        mockMvc.perform(get(BASE_URL + "/2")
                        .param("startDate", START)
                        .param("endDate", END))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName", is("Shreya Gupta")))
                .andExpect(jsonPath("$.pointsByMonth['2024-01']", is(160)))
                .andExpect(jsonPath("$.pointsByMonth['2024-02']", is(140)))
                .andExpect(jsonPath("$.pointsByMonth['2024-03']", is(200)))
                .andExpect(jsonPath("$.totalPoints", is(500)));
    }

    /**
     * Verifies Himanshu's monthly and total points across the full 3-month period.
     * Expected: Jan=550, Feb=60, Mar=495, Total=1105.
     */
    @Test
    @DisplayName("GET /api/rewards/3 returns Himanshu's correct monthly and total points")
    void getRewardsForCustomer_himanshu_returnsCorrectTotal() throws Exception {
        mockMvc.perform(get(BASE_URL + "/3")
                        .param("startDate", START)
                        .param("endDate", END))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName", is("Himanshu Sharma")))
                .andExpect(jsonPath("$.pointsByMonth['2024-01']", is(550)))
                .andExpect(jsonPath("$.pointsByMonth['2024-02']", is(60)))
                .andExpect(jsonPath("$.pointsByMonth['2024-03']", is(495)))
                .andExpect(jsonPath("$.totalPoints", is(1105)));
    }

    /**
     * Verifies Preeyanshu's monthly and total points across the full 3-month period.
     * Expected: Jan=0, Feb=50, Mar=100, Total=150.
     */
    @Test
    @DisplayName("GET /api/rewards/4 returns Preeyanshu's correct monthly and total points")
    void getRewardsForCustomer_preeyanshu_returnsCorrectTotal() throws Exception {
        mockMvc.perform(get(BASE_URL + "/4")
                        .param("startDate", START)
                        .param("endDate", END))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName", is("Preeyanshu Singh")))
                .andExpect(jsonPath("$.pointsByMonth['2024-01']", is(0)))
                .andExpect(jsonPath("$.pointsByMonth['2024-02']", is(50)))
                .andExpect(jsonPath("$.pointsByMonth['2024-03']", is(100)))
                .andExpect(jsonPath("$.totalPoints", is(150)));
    }

    /**
     * Verifies that querying a single month returns only that month's key and correct points.
     */
    @Test
    @DisplayName("GET /api/rewards/1 for January only returns correct January points")
    void getRewardsForCustomer_singleMonth_returnsOnlyThatMonthKey() throws Exception {
        mockMvc.perform(get(BASE_URL + "/1")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pointsByMonth['2024-01']", is(115)))
                .andExpect(jsonPath("$.pointsByMonth['2024-02']").doesNotExist());
    }

    /**
     * Verifies that a two-month range returns exactly two monthly keys.
     */
    @Test
    @DisplayName("GET /api/rewards/2 for two months returns exactly 2 monthly keys")
    void getRewardsForCustomer_twoMonthRange_returns2MonthKeys() throws Exception {
        mockMvc.perform(get(BASE_URL + "/2")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-02-29"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pointsByMonth['2024-01']").exists())
                .andExpect(jsonPath("$.pointsByMonth['2024-02']").exists())
                .andExpect(jsonPath("$.pointsByMonth['2024-03']").doesNotExist());
    }

    /**
     * Verifies that an unknown customer ID returns HTTP 404 with an informative message.
     */
    @Test
    @DisplayName("GET /api/rewards/999 returns 404 for unknown customer")
    void getRewardsForCustomer_unknownId_returns404() throws Exception {
        mockMvc.perform(get(BASE_URL + "/999")
                        .param("startDate", START)
                        .param("endDate", END))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Customer not found with id: 999")));
    }

    /**
     * Verifies that startDate after endDate returns HTTP 400 for a specific customer.
     */
    @Test
    @DisplayName("GET /api/rewards/1 with startDate after endDate returns 400")
    void getRewardsForCustomer_invalidDateRange_returns400() throws Exception {
        mockMvc.perform(get(BASE_URL + "/1")
                        .param("startDate", END)
                        .param("endDate", START))
                .andExpect(status().isBadRequest());
    }

    /**
     * Verifies that a customer with no transactions in the queried range returns zero total points.
     */
    @Test
    @DisplayName("GET /api/rewards/1 with no transactions in range returns zero total points")
    void getRewardsForCustomer_noTransactionsInRange_returnsZeroPoints() throws Exception {
        mockMvc.perform(get(BASE_URL + "/1")
                        .param("startDate", "2023-01-01")
                        .param("endDate", "2023-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPoints", is(0)));
    }

    /**
     * Verifies that missing both date parameters returns HTTP 400.
     */
    @Test
    @DisplayName("GET /api/rewards/1 with no date parameters returns 400")
    void getRewardsForCustomer_noDateParams_returns400() throws Exception {
        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isBadRequest());
    }
}
