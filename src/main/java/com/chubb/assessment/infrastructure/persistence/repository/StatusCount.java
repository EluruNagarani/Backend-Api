package com.chubb.assessment.infrastructure.persistence.repository;

import com.chubb.assessment.domain.models.PolicyStatus;

public interface StatusCount {

    PolicyStatus getStatus();

    long getTotal();
}
