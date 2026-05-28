package com.example.jobplatform.controller;

import com.example.jobplatform.service.JobService;
import com.example.jobplatform.vo.JobSummaryVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JobController.class)
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobService jobService;

    @Test
    void returnsEmptyJobList() throws Exception {
        given(jobService.listJobs(any())).willReturn(List.of());

        mockMvc.perform(get("/api/job/list"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void returnsJobList() throws Exception {
        given(jobService.listJobs(any())).willReturn(List.of(
            new JobSummaryVO(1L, "Java开发", "示例公司", "北京", "15-20K", "本科", "Java,Spring")
        ));

        mockMvc.perform(get("/api/job/list"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].jobName").value("Java开发"));
    }
}
