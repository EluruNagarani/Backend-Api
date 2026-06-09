package com.chubb.assessment.api.dto.request;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record FlagPolicyRequest(
        @NotEmpty List<@NotNull UUID> policyIds) {
}
