package com.example.jobplatform.controller;

import com.example.jobplatform.service.AdminService;
import com.example.jobplatform.service.JobService;
import com.example.jobplatform.service.OperationLogService;
import com.example.jobplatform.vo.DashboardStatsVO;
import com.example.jobplatform.vo.JobDetailVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @MockBean
    private JobService jobService;

    @MockBean
    private OperationLogService operationLogService;

    @Test
    void returnsDashboardStats() throws Exception {
        given(adminService.dashboard()).willReturn(new DashboardStatsVO(100, 50, 20, 3));

        mockMvc.perform(get("/api/admin/dashboard"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.totalJobs").value(100))
            .andExpect(jsonPath("$.data.totalUsers").value(50));
    }

    @Test
    void returns404WhenJobNotFound() throws Exception {
        given(jobService.getJobDetail(999L)).willReturn(null);

        mockMvc.perform(get("/api/admin/job/999"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(404))
            .andExpect(jsonPath("$.message").value("岗位不存在"));
    }

    @Test
    void returnsJobDetailWhenFound() throws Exception {
        given(jobService.getJobDetail(1L)).willReturn(
            new JobDetailVO(1L, "Java开发", "示例公司", "北京", 15, 20, "本科", "应届",
                "Java", "岗位描述", null, 1)
        );

        mockMvc.perform(get("/api/admin/job/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.jobName").value("Java开发"));
    }
}
