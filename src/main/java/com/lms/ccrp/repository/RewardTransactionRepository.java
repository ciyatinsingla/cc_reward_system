package com.lms.ccrp.repository;

import com.lms.ccrp.entity.RequestTransactions;
import com.lms.ccrp.entity.RewardTransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RewardTransactionRepository extends JpaRepository<RewardTransactionHistory, Long> {
    List<RewardTransactionHistory> findByCustomerId(Long customerId);
    @Query(value = "SELECT * FROM reward_transaction_history " +
            "WHERE customerId = :customerId " +
            "AND name = :name " +
            "AND typeOfRequest = :typeOfRequest " +
            "AND rewardDescription = :rewardDescription " +
            "AND numberOfPoints = :numberOfPoints " +
            "AND requesterId = :requesterId ",
            nativeQuery = true)
    List<RewardTransactionHistory> findByAllFields(
            @Param("customerId") Long customerId,
            @Param("name") String name,
            @Param("typeOfRequest") String typeOfRequest,
            @Param("rewardDescription") String rewardDescription,
            @Param("numberOfPoints") Long numberOfPoints,
            @Param("requesterId") String requesterId);
}