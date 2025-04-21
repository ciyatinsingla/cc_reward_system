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

    @Query(value = "SELECT * FROM reward_history " +
            "WHERE customerId = :customerId " +
            "AND name = :name " +
            "AND typeOfRequest = :typeOfRequest " +
            "AND rewardDescription = :rewardDescription " +
            "AND numberOfPoints = :numberOfPoints " +
            "AND requesterId = :requesterId ",
            nativeQuery = true)
    List<RewardHistory> findByAllFields(
            @Param("customerId") Long customerId,
            @Param("name") String name,
            @Param("typeOfRequest") String typeOfRequest,
            @Param("rewardDescription") String rewardDescription,
            @Param("numberOfPoints") Long numberOfPoints,
            @Param("requesterId") String requesterId);

    @Query("SELECT r FROM RewardHistory r WHERE r.transactionTime BETWEEN :startOfDay AND :endOfDay")
    List<RewardHistory> findAllTransactionsForDate(@Param("startOfDay") Date startOfDay, @Param("endOfDay") Date endOfDay);


}