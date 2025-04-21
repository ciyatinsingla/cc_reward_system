package com.lms.ccrp.util;

import com.lms.ccrp.dto.RewardHistoryDTO;
import com.lms.ccrp.entity.RewardHistory;

public class RewardTransactionHistoryMapper {

    public static RewardHistoryDTO entityToRewardTransactionHistoryDTO(RewardHistory entity) {
        if (entity == null) {
            return null;
        }

        return new RewardHistoryDTO(
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

    public static RewardHistory dtoToRewardTransactionHistory(RewardHistoryDTO dto) {
        if (dto == null) {
            return null;
        }

        return RewardHistory.builder()
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
