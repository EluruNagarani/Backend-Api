package com.chubb.assessment.api.dto.response;

import java.util.List;
import java.util.UUID;

public record FlagPolicyResponse(int requested, int flagged, List<UUID> missingIds) {
}
