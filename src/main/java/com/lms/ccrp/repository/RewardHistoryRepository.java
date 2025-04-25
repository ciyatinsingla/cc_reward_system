package com.lms.ccrp.repository;

import com.lms.ccrp.entity.RewardHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface RewardHistoryRepository extends JpaRepository<RewardHistory, Long> {

    List<RewardHistory> findByCustomerId(Long customerId);

    @Query("SELECT r FROM RewardHistory r WHERE r.transactionTime BETWEEN :startOfDay AND :endOfDay ")
    List<RewardHistory> findAllProcessedTransactionsForDate(@Param("startOfDay") Date startOfDay, @Param("endOfDay") Date endOfDay);

}
