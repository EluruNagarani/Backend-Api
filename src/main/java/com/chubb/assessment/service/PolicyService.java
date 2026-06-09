package com.chubb.assessment.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.chubb.assessment.domain.models.LineOfBusiness;
import com.chubb.assessment.domain.models.Policy;
import com.chubb.assessment.domain.models.PolicyFilter;
import com.chubb.assessment.domain.models.PolicyStatistics;
import com.chubb.assessment.domain.models.PolicyStatus;
import com.chubb.assessment.infrastructure.persistence.mapper.EntityToDomain;
import com.chubb.assessment.infrastructure.persistence.repository.LineOfBusinessPremium;
import com.chubb.assessment.infrastructure.persistence.repository.PolicyRepository;
import com.chubb.assessment.infrastructure.persistence.repository.PolicySpecifications;
import com.chubb.assessment.infrastructure.persistence.repository.StatusCount;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PolicyService {

    private static final int EXPIRY_WINDOW_DAYS = 30;

    private final PolicyRepository policyRepository;
    private final PolicySpecifications policySpecifications;
    private final EntityToDomain entityMapper;

    public PolicyService(
            PolicyRepository policyRepository,
            PolicySpecifications policySpecifications,
            EntityToDomain entityMapper) {
        this.policyRepository = policyRepository;
        this.policySpecifications = policySpecifications;
        this.entityMapper = entityMapper;
    }

    
    public Page<Policy> getPolicies(PolicyFilter filter, Pageable pageable) {
        return policyRepository.findAll(policySpecifications.build(filter), pageable)
                .map(entityMapper::toDomain);
    }

    
    public Optional<Policy> getPolicyById(UUID id) {
        return policyRepository.findById(id).map(entityMapper::toDomain);
    }

    @Transactional
    public int flagPoliciesForReview(List<UUID> policyIds) {
        return policyRepository.flagForReview(policyIds);
    }

    public PolicyStatistics getStatistics() {
        return new PolicyStatistics(
                toStatusCounts(policyRepository.countGroupedByStatus()),
                toLineOfBusinessPremiums(policyRepository.totalPremiumGroupedByLineOfBusiness()),
                countExpiringSoon());
    }

    private long countExpiringSoon() {
        LocalDate today = LocalDate.now();
        return policyRepository.countByExpiryDateBetween(today, today.plusDays(EXPIRY_WINDOW_DAYS));
    }

    private Map<PolicyStatus, Long> toStatusCounts(List<StatusCount> rows) {
        return rows.stream()
                .collect(Collectors.toMap(StatusCount::getStatus, StatusCount::getTotal));
    }

    private Map<LineOfBusiness, BigDecimal> toLineOfBusinessPremiums(List<LineOfBusinessPremium> rows) {
        return rows.stream()
                .collect(Collectors.toMap(LineOfBusinessPremium::getLineOfBusiness, LineOfBusinessPremium::getTotal));
    }
}
