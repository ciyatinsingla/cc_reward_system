package com.lms.ccrp.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lms.ccrp.entity.common.Auditable;
import com.lms.ccrp.enums.RewardRequestType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@Entity
@Table(name = "source_transactions")
@NoArgsConstructor
@AllArgsConstructor
public class SourceTransactions extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long customerId;

    private String name;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Temporal(TemporalType.DATE)
    private Date dateOfBirth;

    @Enumerated(EnumType.STRING)
    private RewardRequestType typeOfRequest;

    private String rewardDescription;

    private Long numberOfPoints;

    private String requesterId;

    @Temporal(TemporalType.TIMESTAMP)
    private Date transactionTime;

    private String requestStatus;
    private String reason;

    private boolean isCompleted;

    public void setCompleted() {
        this.isCompleted = true;
    }
}
