package com.lms.ccrp.repository;

import com.lms.ccrp.entity.SourceRewardTransactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SourceRewardTransactionsRepository extends JpaRepository<SourceRewardTransactions, Long> {
}
