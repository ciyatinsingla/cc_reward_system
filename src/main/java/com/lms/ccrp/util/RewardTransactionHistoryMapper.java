package com.lms.ccrp.util;

import com.lms.ccrp.dto.RewardTransactionHistoryDTO;
import com.lms.ccrp.entity.RewardTransactionHistory;

public class RewardTransactionHistoryMapper {

    public static RewardTransactionHistoryDTO entityToRewardTransactionHistoryDTO(RewardTransactionHistory entity) {
        if (entity == null) {
            return null;
        }

        return new RewardTransactionHistoryDTO(
                entity.getCustomerId(),
                entity.getName(),
                entity.getDateOfBirth(),
                entity.getTypeOfRequest(),
                entity.getRewardDescription(),
                entity.getNumberOfPoints(),
                entity.getRequesterId(),
                entity.getTransactionTime()
        );
    }

    public static RewardTransactionHistory dtoToRewardTransactionHistory(RewardTransactionHistoryDTO dto) {
        if (dto == null) {
            return null;
        }

        return RewardTransactionHistory.builder()
                .customerId(dto.getCustomerId())
                .name(dto.getName())
                .dateOfBirth(dto.getDateOfBirth())
                .typeOfRequest(dto.getTypeOfRequest())
                .rewardDescription(dto.getRewardDescription())
                .numberOfPoints(dto.getNumberOfPoints())
                .requesterId(dto.getRequesterId())
                .transactionTime(dto.getTransactionTime())
                .build();
    }
}
