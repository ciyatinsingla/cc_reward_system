package com.lms.ccrp.repository;

import com.lms.ccrp.entity.RequestTransactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestTransactionsRepository extends JpaRepository<RequestTransactions, Long> {
    @Query(value = "SELECT * FROM source_reward_transactions " +
            "WHERE customerId = :customerId " +
            "AND name = :name " +
            "AND typeOfRequest = :typeOfRequest " +
            "AND rewardDescription = :rewardDescription " +
            "AND numberOfPoints = :numberOfPoints " +
            "AND requesterId = :requesterId " +
            "AND requestStatus = :requestStatus " +
            "AND reason = :reason",
            nativeQuery = true)
    List<RequestTransactions> findByAllFields(
            @Param("customerId") Long customerId,
            @Param("name") String name,
            @Param("typeOfRequest") String typeOfRequest,
            @Param("rewardDescription") String rewardDescription,
            @Param("numberOfPoints") Long numberOfPoints,
            @Param("requesterId") String requesterId,
            @Param("requestStatus") String requestStatus,
            @Param("reason") String reason);

}
