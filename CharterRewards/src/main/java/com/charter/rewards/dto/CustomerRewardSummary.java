package com.charter.rewards.dto;

import java.util.Map;

/**
 * What we send back when someone asks for a customer's reward points.
 *
 * The monthly breakdown uses "yyyy-MM" keys (e.g. "2024-01") so it's easy
 * to read. Every month in the requested range is always included — even
 * months where the customer didn't make any purchases (those just show 0).
 */
public class CustomerRewardSummary {

    private final Long customerId;
    private final String customerName;
    private final Map<String, Long> pointsByMonth;
    private final long totalPoints;

    /**
     * Builds the summary with everything the caller needs.
     *
     * @param customerId    who this summary is for
     * @param customerName  their name, so the caller doesn't need a second lookup
     * @param pointsByMonth points earned each month, keyed by "yyyy-MM"
     * @param totalPoints   the sum of all monthly points across the whole period
     */
    public CustomerRewardSummary(Long customerId, String customerName,
                                  Map<String, Long> pointsByMonth, long totalPoints) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.pointsByMonth = pointsByMonth;
        this.totalPoints = totalPoints;
    }

    /**
     * @return the customer's ID
     */
    public Long getCustomerId() {
        return customerId;
    }

    /**
     * @return the customer's full name
     */
    public String getCustomerName() {
        return customerName;
    }

    /**
     * @return month-by-month points breakdown, keyed by "yyyy-MM"
     */
    public Map<String, Long> getPointsByMonth() {
        return pointsByMonth;
    }

    /**
     * @return total points earned across the entire requested period
     */
    public long getTotalPoints() {
        return totalPoints;
    }
}
