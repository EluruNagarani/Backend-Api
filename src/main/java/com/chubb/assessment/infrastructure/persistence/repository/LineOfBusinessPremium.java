package com.chubb.assessment.infrastructure.persistence.repository;

import java.math.BigDecimal;

import com.chubb.assessment.domain.models.LineOfBusiness;

public interface LineOfBusinessPremium {

    LineOfBusiness getLineOfBusiness();

    BigDecimal getTotal();
}
