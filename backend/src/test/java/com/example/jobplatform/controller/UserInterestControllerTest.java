package com.example.jobplatform.controller;

import com.example.jobplatform.exception.GlobalExceptionHandler;
import com.example.jobplatform.service.UserInterestService;
import com.example.jobplatform.vo.InterestJobVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserInterestController.class)
@Import(GlobalExceptionHandler.class)
class UserInterestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserInterestService userInterestService;

    @Test
    void saveFailsValidationWhenJobsEmpty() throws Exception {
        mockMvc.perform(post("/api/user/interest-jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":1,\"jobs\":[]}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void saveInterestJobsSucceeds() throws Exception {
        String body = """
            {
              "userId": 1,
              "jobs": [{ "jobName": "Java开发", "priority": 3 }]
            }
            """;

        mockMvc.perform(post("/api/user/interest-jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        verify(userInterestService).saveInterestJobs(any());
    }

    @Test
    void listInterestJobsReturnsData() throws Exception {
        given(userInterestService.listInterestJobs(1L))
            .willReturn(List.of(new InterestJobVO("Java开发", 3, "manual")));

        mockMvc.perform(get("/api/user/interest-jobs").param("userId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data[0].jobName").value("Java开发"));
    }
}
