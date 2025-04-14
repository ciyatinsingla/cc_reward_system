package com.lms.ccrp.repository;

import com.lms.ccrp.entity.RewardTransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RewardTransactionRepository extends JpaRepository<RewardTransactionHistory, Long> {
    List<RewardTransactionHistory> findByCustomerId(Long customerId);
}