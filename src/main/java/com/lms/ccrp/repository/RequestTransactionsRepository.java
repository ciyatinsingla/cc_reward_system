package com.lms.ccrp.repository;

import com.lms.ccrp.entity.RequestTransactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestTransactionsRepository extends JpaRepository<RequestTransactions, Long> {
    List<RequestTransactions> findByCustomerIdAndIsCompletedTrue(Long customerId);
}
