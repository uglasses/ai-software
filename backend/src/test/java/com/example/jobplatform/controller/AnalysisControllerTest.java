package com.example.jobplatform.controller;

import com.example.jobplatform.service.AnalysisService;
import com.example.jobplatform.vo.ChartItemVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalysisController.class)
class AnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalysisService analysisService;

    @Test
    void returnsCityJobCount() throws Exception {
        given(analysisService.cityJobCount()).willReturn(List.of(new ChartItemVO("北京", 10)));

        mockMvc.perform(get("/api/analysis/city-job-count"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data[0].name").value("北京"))
            .andExpect(jsonPath("$.data[0].value").value(10));
    }

    @Test
    void returnsTopSkillsWithLimit() throws Exception {
        given(analysisService.topSkillCount(5)).willReturn(List.of(new ChartItemVO("Java", 3)));

        mockMvc.perform(get("/api/analysis/top-skills").param("limit", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data[0].name").value("Java"));
    }

    @Test
    void returnsEducationCount() throws Exception {
        given(analysisService.educationRequirementCount()).willReturn(List.of());

        mockMvc.perform(get("/api/analysis/education-count"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").isArray());
    }
}
