package com.td.dgvlm.api.service;

import com.td.dgvlm.api.mapper.TransactionMapper;
import com.td.dgvlm.api.model.TransactionResponse;
import com.td.dgvlm.api.repository.StorTxnRepository;
import com.td.dgvlm.api.entity.StorageTransaction;

import jakarta.persistence.EntityNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionStatusService {

    private final StorTxnRepository txnRepo;
    private final TransactionMapper mapper;

    public TransactionResponse getTransactionStatus(String txnId) {

        if (!StringUtils.hasText(txnId)) {
            log.warn("Empty or null txnId received");
            throw new IllegalArgumentException("Transaction ID must not be empty");
        }

        log.debug("Fetching transaction status for txnId={}", txnId);

        StorageTransaction entity = txnRepo.findByTxnId(txnId)
                .orElseThrow(() -> {
                    log.warn("Transaction not found for txnId={}", txnId);
                    return new EntityNotFoundException(
                            "Transaction not found for transactionId: " + txnId
                    );
                });

        return mapper.toResponse(entity);
    }
}