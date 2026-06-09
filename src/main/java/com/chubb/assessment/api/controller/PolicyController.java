package com.chubb.assessment.api.controller;

import java.time.LocalDate;
import java.util.UUID;

import com.chubb.assessment.api.dto.request.FlagPolicyRequest;
import com.chubb.assessment.api.dto.request.PolicyFilterRequest;
import com.chubb.assessment.api.dto.response.PagedPolicyResponse;
import com.chubb.assessment.api.dto.response.PolicyStatisticsResponse;
import com.chubb.assessment.api.dto.response.PolicySummaryResponse;
import com.chubb.assessment.api.mapper.DomainToResponseDto;
import com.chubb.assessment.api.mapper.RequestDtoToDomain;
import com.chubb.assessment.domain.models.LineOfBusiness;
import com.chubb.assessment.domain.models.Policy;
import com.chubb.assessment.domain.models.PolicyFilter;
import com.chubb.assessment.domain.models.PolicyStatus;
import com.chubb.assessment.service.PolicyService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(PolicyController.BASE_PATH)
@Validated
public class PolicyController {

    static final String BASE_PATH = "/api/v1/policies";

    private static final String PATH_BY_ID = "/{id}";
    private static final String PATH_FLAG = "/flag";
    private static final String PATH_SUMMARY = "/summary";
    private static final String PARAM_ID = "id";
    private static final String PARAM_STATUS = "status";
    private static final String PARAM_LINE_OF_BUSINESS = "lineOfBusiness";
    private static final String PARAM_REGION = "region";
    private static final String PARAM_EFFECTIVE_FROM = "effectiveDateFrom";
    private static final String PARAM_EFFECTIVE_TO = "effectiveDateTo";
    private static final String PARAM_SEARCH = "search";
    private static final int DEFAULT_PAGE_SIZE = 10;

    private final PolicyService policyService;
    private final RequestDtoToDomain requestMapper;
    private final DomainToResponseDto responseMapper;

    public PolicyController(
            PolicyService policyService,
            RequestDtoToDomain requestMapper,
            DomainToResponseDto responseMapper) {
        this.policyService = policyService;
        this.requestMapper = requestMapper;
        this.responseMapper = responseMapper;
    }

    @GetMapping
    public ResponseEntity<PagedPolicyResponse> getPolicies(
            @RequestParam(name = PARAM_STATUS, required = false) PolicyStatus status,
            @RequestParam(name = PARAM_LINE_OF_BUSINESS, required = false) LineOfBusiness lineOfBusiness,
            @RequestParam(name = PARAM_REGION, required = false) String region,
            @RequestParam(name = PARAM_EFFECTIVE_FROM, required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveDateFrom,
            @RequestParam(name = PARAM_EFFECTIVE_TO, required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveDateTo,
            @RequestParam(name = PARAM_SEARCH, required = false) String search,
            @PageableDefault(size = DEFAULT_PAGE_SIZE) Pageable pageable) {
        PolicyFilter filter = requestMapper.toFilter(new PolicyFilterRequest(
                status, lineOfBusiness, region, effectiveDateFrom, effectiveDateTo, search));
        Page<Policy> policies = policyService.getPolicies(filter, pageable);
        return ResponseEntity.ok(responseMapper.toPagedResponse(policies));
    }

    @GetMapping(PATH_BY_ID)
    public ResponseEntity<PolicySummaryResponse> getPolicyById(
            @PathVariable(name = PARAM_ID) UUID id) {
        return policyService.getPolicyById(id)
                .map(responseMapper::toSummaryResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping(PATH_FLAG)
    public ResponseEntity<Void> flagPoliciesForReview(
            @Valid @RequestBody FlagPolicyRequest request) {
        policyService.flagPoliciesForReview(request.policyIds());
        return ResponseEntity.noContent().build();
    }

    @GetMapping(PATH_SUMMARY)
    public ResponseEntity<PolicyStatisticsResponse> getPolicyStatistics() {
        return ResponseEntity.ok(responseMapper.toStatisticsResponse(policyService.getStatistics()));
    }
}
