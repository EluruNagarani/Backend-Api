package com.chubb.assessment.domain.models;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "id")
public class Policy {


    private UUID id;

    private String policyNumber;

    private String policyholderName;

    private LineOfBusiness lineOfBusiness;

    private PolicyStatus status;

    private BigDecimal premiumAmount;

    private String currency;

    private LocalDate effectiveDate;

    private LocalDate expiryDate;

    private String region;

    private String underwriter;

    @Builder.Default
    private boolean flaggedForReview = false;

    private Instant createdAt;

    private Instant updatedAt;
}
