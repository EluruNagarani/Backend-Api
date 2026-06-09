package com.chubb.assessment.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.chubb.assessment.domain.models.FlagResult;
import com.chubb.assessment.domain.models.LineOfBusiness;
import com.chubb.assessment.domain.models.Policy;
import com.chubb.assessment.domain.models.PolicyFilter;
import com.chubb.assessment.domain.models.PolicyStatistics;
import com.chubb.assessment.domain.models.PolicyStatus;
import com.chubb.assessment.infrastructure.persistence.entity.PolicyEntity;
import com.chubb.assessment.infrastructure.persistence.mapper.EntityToDomain;
import com.chubb.assessment.infrastructure.persistence.repository.LineOfBusinessPremium;
import com.chubb.assessment.infrastructure.persistence.repository.PolicyRepository;
import com.chubb.assessment.infrastructure.persistence.repository.PolicySpecifications;
import com.chubb.assessment.infrastructure.persistence.repository.StatusCount;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PolicyService {

    private static final Logger log = LoggerFactory.getLogger(PolicyService.class);

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
    public FlagResult flagPoliciesForReview(List<UUID> policyIds) {
        long start = System.currentTimeMillis();
        List<PolicyEntity> found = policyRepository.findAllById(policyIds);
        found.forEach(entity -> entity.setFlaggedForReview(true));
        policyRepository.saveAll(found);
        FlagResult result = new FlagResult(policyIds.size(), found.size(), missingIds(policyIds, found));
        log.info("method=flagPoliciesForReview requested={} flagged={} durationMs={}",
                result.requested(), result.flagged(), System.currentTimeMillis() - start);
        return result;
    }

    private List<UUID> missingIds(List<UUID> requested, List<PolicyEntity> found) {
        Set<UUID> foundIds = found.stream()
                .map(PolicyEntity::getId)
                .collect(Collectors.toSet());
        return requested.stream()
                .filter(id -> !foundIds.contains(id))
                .toList();
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
