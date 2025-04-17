package com.lms.ccrp.entity;

import com.lms.ccrp.entity.common.Auditable;
import com.lms.ccrp.enums.RequestType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@Entity
@Table(name = "source_reward_transactions")
@NoArgsConstructor
@AllArgsConstructor
public class SourceRewardTransactions extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long customerId;

    private String name;

    private Date dateOfBirth;

    @Enumerated(EnumType.STRING)
    private RequestType typeOfRequest;

    private String rewardDescription;

    private Long numberOfPoints;

    private String requesterId;

    @Temporal(TemporalType.TIMESTAMP)
    private Date transactionTime;

    private String requestStatus;
    private String reason;

    private boolean isCompleted;
}
