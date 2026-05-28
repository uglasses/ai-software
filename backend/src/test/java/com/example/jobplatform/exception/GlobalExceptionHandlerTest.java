package com.example.jobplatform.exception;

import com.example.jobplatform.common.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleIllegalArgument_returns400WithMessage() {
        ApiResponse<Void> response = handler.handleIllegalArgument(new IllegalArgumentException("用户名或密码错误"));

        assertThat(response.code()).isEqualTo(400);
        assertThat(response.message()).isEqualTo("用户名或密码错误");
        assertThat(response.data()).isNull();
    }

    @Test
    void handleServiceUnavailable_returns503() {
        ApiResponse<Void> response = handler.handleServiceUnavailable(
            new ServiceUnavailableException("请配置 DEEPSEEK_API_KEY"));

        assertThat(response.code()).isEqualTo(503);
        assertThat(response.message()).contains("DEEPSEEK_API_KEY");
    }

    @Test
    void handleDeepseek_returns502() {
        ApiResponse<Void> response = handler.handleDeepseek(new DeepseekException("模型调用失败"));

        assertThat(response.code()).isEqualTo(502);
        assertThat(response.message()).isEqualTo("模型调用失败");
    }

    @Test
    void handleException_returns500WithGenericMessage() {
        ApiResponse<Void> response = handler.handleException(new RuntimeException("unexpected"));

        assertThat(response.code()).isEqualTo(500);
        assertThat(response.message()).isEqualTo("系统内部错误");
    }
}
