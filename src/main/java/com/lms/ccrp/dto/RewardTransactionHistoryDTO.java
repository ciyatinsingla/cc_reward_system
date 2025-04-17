package com.lms.ccrp.dto;

import com.lms.ccrp.enums.RequestType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RewardTransactionHistoryDTO {

    @NonNull
    private Long customerId;

    @NonNull
    private String name;

    @NonNull
    private Date dateOfBirth;

    @NonNull
    @Enumerated(EnumType.STRING)
    private RequestType typeOfRequest;

    @NonNull
    private String rewardDescription;

    @NonNull
    private Long numberOfPoints;

    private String requesterId;

    @Temporal(TemporalType.TIMESTAMP)
    private Date transactionTime;
}
