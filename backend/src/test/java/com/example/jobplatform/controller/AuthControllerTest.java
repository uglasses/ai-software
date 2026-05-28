package com.example.jobplatform.controller;

import com.example.jobplatform.dto.LoginRequestDTO;
import com.example.jobplatform.dto.RegisterRequestDTO;
import com.example.jobplatform.exception.GlobalExceptionHandler;
import com.example.jobplatform.service.AuthService;
import com.example.jobplatform.service.OperationLogService;
import com.example.jobplatform.vo.AuthUserVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private OperationLogService operationLogService;

    @Test
    void loginValidationFailsWhenBodyEmpty() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void loginSucceeds() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setIdentifier("student");
        request.setPassword("secret12");

        AuthUserVO user = new AuthUserVO(1L, "student", null, null, LocalDateTime.now());
        given(authService.login(any(LoginRequestDTO.class))).willReturn(user);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.username").value("student"));

        verify(operationLogService).logLogin(eq(1L), eq("student"), eq(true), any(), isNull());
    }

    @Test
    void registerSucceeds() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUsername("newuser");
        request.setPassword("pass123");

        AuthUserVO user = new AuthUserVO(2L, "newuser", null, null, null);
        given(authService.register(any(RegisterRequestDTO.class))).willReturn(user);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.username").value("newuser"));
    }
}
