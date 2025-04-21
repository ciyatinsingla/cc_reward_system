package com.lms.ccrp.repository;

import com.lms.ccrp.entity.SourceTransactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SourceTransactionsRepository extends JpaRepository<SourceTransactions, Long> {
    List<SourceTransactions> findByCustomerIdAndIsCompletedTrue(Long customerId);
}
