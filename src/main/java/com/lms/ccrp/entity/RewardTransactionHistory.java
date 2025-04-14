package com.lms.ccrp.entity;

import com.lms.ccrp.enums.RequestType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Builder
@Entity
@Table(name = "reward_transaction_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RewardTransactionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long customerId;

    private String name;

    private Date dateOfBirth;

    @Enumerated(EnumType.STRING)
    private RequestType typeOfRequest;

    private String rewardDescription;

    private int numberOfPoints;

    private String requesterId;

    @Temporal(TemporalType.TIMESTAMP)
    private Date transactionTime;
}
