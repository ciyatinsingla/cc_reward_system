package com.lms.ccrp.dto;

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
public class CustomerDTO {

    @NonNull
    private String name;

    @NonNull
    private Long creditCardNumber;

    @NonNull
    private String creditCardType;

    @NonNull
    @Temporal(TemporalType.TIMESTAMP)
    private Date cardIssuanceDate;
}
