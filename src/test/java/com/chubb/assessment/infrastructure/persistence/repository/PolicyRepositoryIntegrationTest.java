package com.chubb.assessment.infrastructure.persistence.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.chubb.assessment.domain.models.LineOfBusiness;
import com.chubb.assessment.domain.models.PolicyFilter;
import com.chubb.assessment.domain.models.PolicyStatus;
import com.chubb.assessment.infrastructure.persistence.entity.PolicyEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@TestPropertySource(properties = "spring.sql.init.mode=never")
class PolicyRepositoryIntegrationTest {

    @Autowired
    private PolicyRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    private final PolicySpecifications specifications = new PolicySpecifications();

    @BeforeEach
    void seedPolicies() {
        persist("POL-000001", "Wei Lin Tan", "Mei Chen", LineOfBusiness.PROPERTY,
                PolicyStatus.ACTIVE, "Singapore", LocalDate.of(2025, 7, 1), "100.00");
        persist("POL-000002", "Hiroshi Tanaka", "Kenji Sato", LineOfBusiness.MARINE,
                PolicyStatus.ACTIVE, "Japan", LocalDate.of(2025, 6, 20), "200.00");
        persist("POL-000003", "Siti Nurhaliza", "Aisyah Rahman", LineOfBusiness.CASUALTY,
                PolicyStatus.PENDING, "Malaysia", LocalDate.of(2026, 5, 15), "300.00");
        persist("POL-000004", "James Wong", "Carmen Lau", LineOfBusiness.PROPERTY,
                PolicyStatus.EXPIRED, "Hong Kong", LocalDate.of(2024, 6, 1), "400.00");
        entityManager.flush();
    }

    @Test
    void findAll_withEmptyFilter_returnsEveryPolicy() {
        long count = repository.findAll(specifications.build(emptyFilter()), PageRequest.of(0, 10))
                .getTotalElements();

        assertEquals(4, count);
    }

    @Test
    void findAll_filteredByStatus_returnsOnlyMatchingStatus() {
        PolicyFilter filter = new PolicyFilter(PolicyStatus.ACTIVE, null, null, null, null, null);

        List<PolicyEntity> result = repository.findAll(specifications.build(filter));

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(p -> p.getStatus() == PolicyStatus.ACTIVE));
    }

    @Test
    void findAll_filteredByLineOfBusiness_returnsOnlyMatchingLine() {
        PolicyFilter filter = new PolicyFilter(null, LineOfBusiness.PROPERTY, null, null, null, null);

        List<PolicyEntity> result = repository.findAll(specifications.build(filter));

        assertEquals(2, result.size());
    }

    @Test
    void findAll_filteredByRegion_matchesExactRegion() {
        PolicyFilter filter = new PolicyFilter(null, null, "Japan", null, null, null);

        List<PolicyEntity> result = repository.findAll(specifications.build(filter));

        assertEquals(1, result.size());
        assertEquals("POL-000002", result.get(0).getPolicyNumber());
    }

    @Test
    void findAll_filteredByEffectiveDateRange_returnsPoliciesWithinBounds() {
        PolicyFilter filter = new PolicyFilter(null, null, null,
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31), null);

        List<PolicyEntity> result = repository.findAll(specifications.build(filter));

        assertEquals(2, result.size());
    }

    @Test
    void findAll_searchByPartialHolderNameIgnoringCase_matchesAcrossTextFields() {
        PolicyFilter filter = new PolicyFilter(null, null, null, null, null, "tan");

        List<PolicyEntity> result = repository.findAll(specifications.build(filter));

        assertEquals(2, result.size());
    }

    @Test
    void findAll_searchByUnderwriter_matchesUnderwriterField() {
        PolicyFilter filter = new PolicyFilter(null, null, null, null, null, "kenji");

        List<PolicyEntity> result = repository.findAll(specifications.build(filter));

        assertEquals(1, result.size());
        assertEquals("POL-000002", result.get(0).getPolicyNumber());
    }

    @Test
    void countGroupedByStatus_acrossSeededData_countsEachStatus() {
        Map<PolicyStatus, Long> counts = repository.countGroupedByStatus().stream()
                .collect(Collectors.toMap(StatusCount::getStatus, StatusCount::getTotal));

        assertEquals(2L, counts.get(PolicyStatus.ACTIVE));
        assertEquals(1L, counts.get(PolicyStatus.PENDING));
        assertEquals(1L, counts.get(PolicyStatus.EXPIRED));
    }

    @Test
    void totalPremiumGroupedByLineOfBusiness_acrossSeededData_sumsPremiums() {
        Map<LineOfBusiness, BigDecimal> totals =
                repository.totalPremiumGroupedByLineOfBusiness().stream()
                        .collect(Collectors.toMap(
                                LineOfBusinessPremium::getLineOfBusiness,
                                LineOfBusinessPremium::getTotal));

        assertEquals(0, new BigDecimal("500.00").compareTo(totals.get(LineOfBusiness.PROPERTY)));
        assertEquals(0, new BigDecimal("200.00").compareTo(totals.get(LineOfBusiness.MARINE)));
    }

    @Test
    void countByExpiryDateBetween_withinWindow_countsOnlyPoliciesExpiringInRange() {
        LocalDate base = LocalDate.of(2030, 1, 1);
        persist("POL-EXP-1", "Soon Expiring", "Underwriter", LineOfBusiness.CASUALTY,
                PolicyStatus.ACTIVE, "Region", base.minusYears(1), "10.00", base.plusDays(10));
        entityManager.flush();

        long count = repository.countByExpiryDateBetween(base, base.plusDays(30));

        assertEquals(1, count);
    }

    private PolicyFilter emptyFilter() {
        return new PolicyFilter(null, null, null, null, null, null);
    }

    private void persist(String number, String holder, String underwriter, LineOfBusiness line,
                         PolicyStatus status, String region, LocalDate effective, String premium) {
        persist(number, holder, underwriter, line, status, region, effective, premium,
                effective.plusYears(1));
    }

    private void persist(String number, String holder, String underwriter, LineOfBusiness line,
                         PolicyStatus status, String region, LocalDate effective, String premium,
                         LocalDate expiry) {
        entityManager.persist(PolicyEntity.builder()
                .id(UUID.randomUUID())
                .policyNumber(number)
                .policyholderName(holder)
                .underwriter(underwriter)
                .lineOfBusiness(line)
                .status(status)
                .region(region)
                .effectiveDate(effective)
                .expiryDate(expiry)
                .premiumAmount(new BigDecimal(premium))
                .currency("SGD")
                .build());
    }
}
