package com.chubb.assessment.domain.models;

import java.util.List;
import java.util.UUID;

public record FlagResult(int requested, int flagged, List<UUID> missingIds) {
}
