package com.charter.rewards.controller;

import com.charter.rewards.dto.CustomerRewardSummary;
import com.charter.rewards.service.RewardsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.util.List;

/**
 * Handles all the reward-related HTTP requests.
 *
 * Two endpoints live here — one to pull rewards for every customer,
 * and one to drill into a specific customer. Both need a date range
 * in yyyy-MM-dd format so we know which transactions to look at.
 */
@RestController
@RequestMapping("/api/rewards")
public class RewardsController {

    private final RewardsService rewardsService;

    /**
     * Spring injects the service here — no need to new it up manually.
     *
     * @param rewardsService does the actual points calculation work
     */
    public RewardsController(RewardsService rewardsService) {
        this.rewardsService = rewardsService;
    }

    /**
     * Returns reward summaries for every customer who had at least one
     * transaction in the given date range.
     *
     * Each summary breaks points down by month and includes a grand total.
     * Months with zero activity still show up in the response — they just
     * have 0 points.
     *
     * @param startDate first day of the period you want to look at (e.g. 2024-01-01)
     * @param endDate   last day of the period (e.g. 2024-03-31)
     * @return 200 with the list of summaries, or 400 if the dates don't make sense
     */
    @GetMapping
    public ResponseEntity<List<CustomerRewardSummary>> getAllRewards(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return ResponseEntity.ok(rewardsService.calculateRewardsForAll(startDate, endDate));
    }

    /**
     * Returns the reward summary for one specific customer over the given date range.
     *
     * Same structure as the all-customers endpoint — monthly breakdown plus total.
     * If the customer ID doesn't exist you'll get a 404 back.
     *
     * @param customerId the ID of the customer you're looking up
     * @param startDate  first day of the period
     * @param endDate    last day of the period
     * @return 200 with the customer's summary, 404 if they don't exist,
     *         or 400 if the dates are invalid
     */
    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerRewardSummary> getRewardsForCustomer(
            @PathVariable Long customerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return ResponseEntity.ok(rewardsService.calculateRewardsForCustomer(customerId, startDate, endDate));
    }
}
