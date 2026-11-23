package com.ledgercore.repository;

import com.ledgercore.model.TransactionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRecordRepository extends JpaRepository<TransactionRecord, Long> {
    List<TransactionRecord> findByFromAccountIdOrToAccountIdOrderByCreatedAtDescIdDesc(Long fromAccountId, Long toAccountId);
}
