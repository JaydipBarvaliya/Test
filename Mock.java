package com.td.dgvlm.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RetryClaimService {

    private final StorTxnRepository repo;

    @Transactional
    public List<String> claimErrorTransactions(int batchSize) {

        log.info("Starting claimErrorTransactions. Batch size: {}", batchSize);

        // Step 1: Lock ERROR rows safely using FOR UPDATE SKIP LOCKED
        List<String> ids = repo.lockErrorTransactions()
                .stream()
                .limit(batchSize)
                .toList();

        if (ids.isEmpty()) {
            log.info("No ERROR transactions available to claim.");
            return ids;
        }

        log.info("Locked {} ERROR transactions: {}", ids.size(), ids);

        // Step 2: Move ERROR -> ACTIVE so they are not re-selected
        repo.markActive(ids);

        log.info("Marked {} transactions as ACTIVE.", ids.size());

        // Transaction commits automatically when method exits
        log.info("Transaction committed. Returning claimed transaction IDs.");

        return ids;
    }
}