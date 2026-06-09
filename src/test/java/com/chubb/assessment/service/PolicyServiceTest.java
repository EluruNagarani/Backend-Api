package com.chubb.assessment.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PolicyServiceTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private PolicySpecifications policySpecifications;

    @Mock
    private EntityToDomain entityMapper;

    @InjectMocks
    private PolicyService policyService;

    @Test
    void getPolicies_withFilter_returnsMappedDomainPage() {
        PolicyFilter filter = new PolicyFilter(null, null, null, null, null, null);
        Pageable pageable = PageRequest.of(0, 10);
        PolicyEntity entity = entityWithId(UUID.randomUUID());
        Policy domain = Policy.builder().id(entity.getId()).build();
        Specification<PolicyEntity> spec = (root, query, cb) -> null;

        when(policySpecifications.build(filter)).thenReturn(spec);
        when(policyRepository.findAll(eq(spec), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(entity), pageable, 1));
        when(entityMapper.toDomain(entity)).thenReturn(domain);

        Page<Policy> result = policyService.getPolicies(filter, pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(domain, result.getContent().get(0));
    }

    @Test
    void getPolicyById_whenPresent_returnsMappedDomain() {
        UUID id = UUID.randomUUID();
        PolicyEntity entity = entityWithId(id);
        Policy domain = Policy.builder().id(id).build();
        when(policyRepository.findById(id)).thenReturn(Optional.of(entity));
        when(entityMapper.toDomain(entity)).thenReturn(domain);

        Optional<Policy> result = policyService.getPolicyById(id);

        assertTrue(result.isPresent());
        assertSame(domain, result.get());
    }

    @Test
    void getPolicyById_whenMissing_returnsEmpty() {
        UUID id = UUID.randomUUID();
        when(policyRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Policy> result = policyService.getPolicyById(id);

        assertTrue(result.isEmpty());
    }

    @Test
    void flagPoliciesForReview_whenAllFound_flagsEveryEntityAndReportsNoMissing() {
        UUID idOne = UUID.randomUUID();
        UUID idTwo = UUID.randomUUID();
        PolicyEntity entityOne = entityWithId(idOne);
        PolicyEntity entityTwo = entityWithId(idTwo);
        when(policyRepository.findAllById(List.of(idOne, idTwo)))
                .thenReturn(List.of(entityOne, entityTwo));

        FlagResult result = policyService.flagPoliciesForReview(List.of(idOne, idTwo));

        assertEquals(2, result.requested());
        assertEquals(2, result.flagged());
        assertTrue(result.missingIds().isEmpty());
        assertTrue(entityOne.isFlaggedForReview());
        assertTrue(entityTwo.isFlaggedForReview());
        verify(policyRepository).saveAll(List.of(entityOne, entityTwo));
    }

    @Test
    void flagPoliciesForReview_whenSomeNotFound_reportsMissingIds() {
        UUID found = UUID.randomUUID();
        UUID missing = UUID.randomUUID();
        PolicyEntity entity = entityWithId(found);
        when(policyRepository.findAllById(List.of(found, missing)))
                .thenReturn(List.of(entity));

        FlagResult result = policyService.flagPoliciesForReview(List.of(found, missing));

        assertEquals(2, result.requested());
        assertEquals(1, result.flagged());
        assertEquals(List.of(missing), result.missingIds());
    }

    @Test
    void flagPoliciesForReview_whenNoneFound_flagsNothing() {
        UUID missing = UUID.randomUUID();
        when(policyRepository.findAllById(List.of(missing))).thenReturn(List.of());

        FlagResult result = policyService.flagPoliciesForReview(List.of(missing));

        assertEquals(1, result.requested());
        assertEquals(0, result.flagged());
        assertEquals(List.of(missing), result.missingIds());
        verify(policyRepository).saveAll(List.of());
    }

    @Test
    void getStatistics_withGroupedRows_assemblesStatisticsFromRepository() {
        when(policyRepository.countGroupedByStatus())
                .thenReturn(List.of(statusCount(PolicyStatus.ACTIVE, 3L)));
        when(policyRepository.totalPremiumGroupedByLineOfBusiness())
                .thenReturn(List.of(linePremium(LineOfBusiness.PROPERTY, new BigDecimal("100.00"))));
        when(policyRepository.countByExpiryDateBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(5L);

        PolicyStatistics statistics = policyService.getStatistics();

        assertEquals(Map.of(PolicyStatus.ACTIVE, 3L), statistics.countsByStatus());
        assertEquals(new BigDecimal("100.00"),
                statistics.totalPremiumByLineOfBusiness().get(LineOfBusiness.PROPERTY));
        assertEquals(5L, statistics.expiringSoonCount());
    }

    @Test
    void getStatistics_forExpiringWindow_queriesNextThirtyDays() {
        when(policyRepository.countGroupedByStatus()).thenReturn(List.of());
        when(policyRepository.totalPremiumGroupedByLineOfBusiness()).thenReturn(List.of());
        when(policyRepository.countByExpiryDateBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(0L);

        policyService.getStatistics();

        ArgumentCaptor<LocalDate> from = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> to = ArgumentCaptor.forClass(LocalDate.class);
        verify(policyRepository).countByExpiryDateBetween(from.capture(), to.capture());
        assertEquals(30, java.time.temporal.ChronoUnit.DAYS.between(from.getValue(), to.getValue()));
        assertFalse(from.getValue().isAfter(to.getValue()));
    }

    private PolicyEntity entityWithId(UUID id) {
        return PolicyEntity.builder().id(id).flaggedForReview(false).build();
    }

    private StatusCount statusCount(PolicyStatus status, long total) {
        return new StatusCount() {
            @Override
            public PolicyStatus getStatus() {
                return status;
            }

            @Override
            public long getTotal() {
                return total;
            }
        };
    }

    private LineOfBusinessPremium linePremium(LineOfBusiness line, BigDecimal total) {
        return new LineOfBusinessPremium() {
            @Override
            public LineOfBusiness getLineOfBusiness() {
                return line;
            }

            @Override
            public BigDecimal getTotal() {
                return total;
            }
        };
    }
}
