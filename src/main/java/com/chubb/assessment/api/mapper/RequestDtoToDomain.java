package com.chubb.assessment.api.mapper;

import com.chubb.assessment.api.dto.request.PolicyFilterRequest;
import com.chubb.assessment.domain.models.PolicyFilter;

import org.springframework.stereotype.Component;

@Component
public class RequestDtoToDomain {

    public PolicyFilter toFilter(PolicyFilterRequest request) {
        return new PolicyFilter(
                request.status(),
                request.lineOfBusiness(),
                request.region(),
                request.effectiveDateFrom(),
                request.effectiveDateTo(),
                request.search());
    }
}
