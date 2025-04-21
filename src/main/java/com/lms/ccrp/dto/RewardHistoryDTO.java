package com.lms.ccrp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lms.ccrp.enums.RewardRequestType;
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
public class RewardHistoryDTO {

    @NonNull
    private Long customerId;

    @NonNull
    private String name;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Temporal(TemporalType.DATE)
    private Date dateOfBirth;

    @NonNull
    @Enumerated(EnumType.STRING)
    private RewardRequestType typeOfRequest;

    @NonNull
    private String rewardDescription;

    @NonNull
    private Long numberOfPoints;

    private String requesterId;

    @Temporal(TemporalType.TIMESTAMP)
    private Date transactionTime;
}
