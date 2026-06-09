package com.chubb.assessment.api.dto.response;

import java.util.List;

public record PagedPolicyResponse(
        List<PolicySummaryResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {
}
