package com.chubb.assessment.infrastructure.persistence.repository;

import java.time.LocalDate;

import com.chubb.assessment.domain.models.LineOfBusiness;
import com.chubb.assessment.domain.models.PolicyFilter;
import com.chubb.assessment.domain.models.PolicyStatus;
import com.chubb.assessment.infrastructure.persistence.entity.PolicyEntity;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class PolicySpecifications {

    private static final String FIELD_STATUS = "status";
    private static final String FIELD_LINE_OF_BUSINESS = "lineOfBusiness";
    private static final String FIELD_REGION = "region";
    private static final String FIELD_EFFECTIVE_DATE = "effectiveDate";
    private static final String FIELD_POLICY_NUMBER = "policyNumber";
    private static final String FIELD_POLICYHOLDER_NAME = "policyholderName";
    private static final String FIELD_UNDERWRITER = "underwriter";
    private static final String WILDCARD = "%";

    public Specification<PolicyEntity> build(PolicyFilter filter) {
        return Specification.allOf(
                hasStatus(filter.status()),
                hasLineOfBusiness(filter.lineOfBusiness()),
                hasRegion(filter.region()),
                effectiveOnOrAfter(filter.effectiveDateFrom()),
                effectiveOnOrBefore(filter.effectiveDateTo()),
                matchesSearch(filter.search()));
    }

    private Specification<PolicyEntity> hasStatus(PolicyStatus status) {
        return (root, query, cb) ->
                status == null ? cb.conjunction() : cb.equal(root.get(FIELD_STATUS), status);
    }

    private Specification<PolicyEntity> hasLineOfBusiness(LineOfBusiness lineOfBusiness) {
        return (root, query, cb) ->
                lineOfBusiness == null ? cb.conjunction() : cb.equal(root.get(FIELD_LINE_OF_BUSINESS), lineOfBusiness);
    }

    private Specification<PolicyEntity> hasRegion(String region) {
        return (root, query, cb) ->
                !StringUtils.hasText(region) ? cb.conjunction() : cb.equal(root.get(FIELD_REGION), region);
    }

    private Specification<PolicyEntity> effectiveOnOrAfter(LocalDate from) {
        return (root, query, cb) ->
                from == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get(FIELD_EFFECTIVE_DATE), from);
    }

    private Specification<PolicyEntity> effectiveOnOrBefore(LocalDate to) {
        return (root, query, cb) ->
                to == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get(FIELD_EFFECTIVE_DATE), to);
    }

    private Specification<PolicyEntity> matchesSearch(String search) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(search)) {
                return cb.conjunction();
            }
            String pattern = WILDCARD + search.toLowerCase() + WILDCARD;
            return cb.or(
                    cb.like(cb.lower(root.get(FIELD_POLICY_NUMBER)), pattern),
                    cb.like(cb.lower(root.get(FIELD_POLICYHOLDER_NAME)), pattern),
                    cb.like(cb.lower(root.get(FIELD_UNDERWRITER)), pattern));
        };
    }
}
