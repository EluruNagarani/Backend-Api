package com.chubb.assessment.infrastructure.persistence.mapper;

import com.chubb.assessment.domain.models.Policy;
import com.chubb.assessment.infrastructure.persistence.entity.PolicyEntity;

import org.springframework.stereotype.Component;

@Component
public class EntityToDomain {

    public Policy toDomain(PolicyEntity entity) {
        return Policy.builder()
                .id(entity.getId())
                .policyNumber(entity.getPolicyNumber())
                .policyholderName(entity.getPolicyholderName())
                .lineOfBusiness(entity.getLineOfBusiness())
                .status(entity.getStatus())
                .premiumAmount(entity.getPremiumAmount())
                .currency(entity.getCurrency())
                .effectiveDate(entity.getEffectiveDate())
                .expiryDate(entity.getExpiryDate())
                .region(entity.getRegion())
                .underwriter(entity.getUnderwriter())
                .flaggedForReview(entity.isFlaggedForReview())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public PolicyEntity toEntity(Policy policy) {
        return PolicyEntity.builder()
                .id(policy.getId())
                .policyNumber(policy.getPolicyNumber())
                .policyholderName(policy.getPolicyholderName())
                .lineOfBusiness(policy.getLineOfBusiness())
                .status(policy.getStatus())
                .premiumAmount(policy.getPremiumAmount())
                .currency(policy.getCurrency())
                .effectiveDate(policy.getEffectiveDate())
                .expiryDate(policy.getExpiryDate())
                .region(policy.getRegion())
                .underwriter(policy.getUnderwriter())
                .flaggedForReview(policy.isFlaggedForReview())
                .createdAt(policy.getCreatedAt())
                .updatedAt(policy.getUpdatedAt())
                .build();
    }
}
