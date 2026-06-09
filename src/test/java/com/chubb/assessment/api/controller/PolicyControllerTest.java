package com.chubb.assessment.api.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.chubb.assessment.api.dto.response.FlagPolicyResponse;
import com.chubb.assessment.api.dto.response.MoneyResponse;
import com.chubb.assessment.api.dto.response.PagedPolicyResponse;
import com.chubb.assessment.api.dto.response.PolicyStatisticsResponse;
import com.chubb.assessment.api.dto.response.PolicyStatusResponse;
import com.chubb.assessment.api.dto.response.PolicySummaryResponse;
import com.chubb.assessment.api.mapper.DomainToResponseDto;
import com.chubb.assessment.api.mapper.RequestDtoToDomain;
import com.chubb.assessment.domain.models.FlagResult;
import com.chubb.assessment.domain.models.Policy;
import com.chubb.assessment.domain.models.PolicyFilter;
import com.chubb.assessment.domain.models.PolicyStatistics;
import com.chubb.assessment.service.PolicyService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PolicyController.class)
class PolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PolicyService policyService;

    @MockitoBean
    private RequestDtoToDomain requestMapper;

    @MockitoBean
    private DomainToResponseDto responseMapper;

    @Test
    void getPolicies_withDefaults_returnsOkWithPagedBody() throws Exception {
        PolicyFilter filter = new PolicyFilter(null, null, null, null, null, null);
        Page<Policy> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(requestMapper.toFilter(any())).thenReturn(filter);
        when(policyService.getPolicies(any(PolicyFilter.class), any(Pageable.class))).thenReturn(page);
        when(responseMapper.toPagedResponse(page))
                .thenReturn(new PagedPolicyResponse(List.of(), 0, 10, 0, 0));

        mockMvc.perform(get(PolicyController.BASE_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.size").value(10));
    }

    @Test
    void getPolicies_withInvalidStatus_returnsBadRequest() throws Exception {
        mockMvc.perform(get(PolicyController.BASE_PATH).param("status", "NOT_A_STATUS"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getPolicyById_whenFound_returnsOkWithSummary() throws Exception {
        UUID id = UUID.randomUUID();
        Policy policy = Policy.builder().id(id).build();
        when(policyService.getPolicyById(id)).thenReturn(Optional.of(policy));
        when(responseMapper.toSummaryResponse(policy)).thenReturn(summaryResponse());

        mockMvc.perform(get(PolicyController.BASE_PATH + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.policyNumber").value("POL-1"));
    }

    @Test
    void getPolicyById_whenMissing_returnsNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(policyService.getPolicyById(id)).thenReturn(Optional.empty());

        mockMvc.perform(get(PolicyController.BASE_PATH + "/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPolicyById_withMalformedId_returnsBadRequest() throws Exception {
        mockMvc.perform(get(PolicyController.BASE_PATH + "/not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void flagPoliciesForReview_withValidIds_returnsOkWithFlagSummary() throws Exception {
        UUID id = UUID.randomUUID();
        when(policyService.flagPoliciesForReview(List.of(id)))
                .thenReturn(new FlagResult(1, 1, List.of()));
        when(responseMapper.toFlagResponse(any(FlagResult.class)))
                .thenReturn(new FlagPolicyResponse(1, 1, List.of()));

        mockMvc.perform(patch(PolicyController.BASE_PATH + "/flag")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"policyIds\":[\"" + id + "\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flagged").value(1));
    }

    @Test
    void flagPoliciesForReview_withEmptyIds_returnsBadRequest() throws Exception {
        mockMvc.perform(patch(PolicyController.BASE_PATH + "/flag")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"policyIds\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getPolicyStatistics_whenRequested_returnsOkWithStatistics() throws Exception {
        PolicyStatistics statistics = new PolicyStatistics(java.util.Map.of(), java.util.Map.of(), 4L);
        when(policyService.getStatistics()).thenReturn(statistics);
        when(responseMapper.toStatisticsResponse(statistics))
                .thenReturn(new PolicyStatisticsResponse(java.util.Map.of(), java.util.Map.of(), 4L));

        mockMvc.perform(get(PolicyController.BASE_PATH + "/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expiringSoonCount").value(4));
    }

    private PolicySummaryResponse summaryResponse() {
        return new PolicySummaryResponse(
                "POL-1",
                "Holder",
                "Singapore",
                PolicyStatusResponse.ACTIVE,
                new MoneyResponse(new BigDecimal("100.00"), "SGD"),
                LocalDate.now(),
                LocalDate.now().plusDays(10),
                true);
    }
}
