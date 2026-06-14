package com.example.jobplatform.llm;

import com.example.jobplatform.config.DeepseekProperties;
import com.example.jobplatform.exception.DeepseekException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.POST;

class DeepseekChatClientTest {

    private static final String BASE_URL = "https://api.deepseek.com";

    private MockRestServiceServer server;
    private DeepseekChatClient chatClient;
    private DeepseekProperties properties;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        properties = new DeepseekProperties();
        properties.setModel("deepseek-chat");
        RestClient client = builder.baseUrl(BASE_URL).build();
        chatClient = new DeepseekChatClient(client, new ObjectMapper(), properties);
    }

    @Test
    void chatCompletion_returnsTrimmedContent() {
        server.expect(requestTo(BASE_URL + "/v1/chat/completions"))
            .andExpect(method(POST))
            .andExpect(header("Authorization", "Bearer sk-test"))
            .andRespond(withSuccess(
                "{\"choices\":[{\"message\":{\"content\":\"  建议内容  \"}}]}",
                MediaType.APPLICATION_JSON
            ));

        String result = chatClient.chatCompletion("deepseek-chat", "sk-test", "system", "user");

        assertThat(result).isEqualTo("建议内容");
        server.verify();
    }

    @Test
    void chatCompletion_usesDefaultModelWhenNull() {
        server.expect(requestTo(BASE_URL + "/v1/chat/completions"))
            .andExpect(method(POST))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("\"model\":\"deepseek-chat\"")))
            .andRespond(withSuccess(
                "{\"choices\":[{\"message\":{\"content\":\"ok\"}}]}",
                MediaType.APPLICATION_JSON
            ));

        String result = chatClient.chatCompletion(null, "sk-test", "system", "user");

        assertThat(result).isEqualTo("ok");
        server.verify();
    }

    @Test
    void chatCompletion_throwsOnApiErrorInBody() {
        server.expect(requestTo(BASE_URL + "/v1/chat/completions"))
            .andRespond(withSuccess(
                "{\"error\":{\"message\":\"invalid key\"}}",
                MediaType.APPLICATION_JSON
            ));

        assertThatThrownBy(() -> chatClient.chatCompletion("deepseek-chat", "sk-bad", "s", "u"))
            .isInstanceOf(DeepseekException.class)
            .hasMessageContaining("API 错误")
            .hasMessageContaining("invalid key");
    }

    @Test
    void chatCompletion_throwsOnEmptyChoices() {
        server.expect(requestTo(BASE_URL + "/v1/chat/completions"))
            .andRespond(withSuccess("{\"choices\":[]}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> chatClient.chatCompletion("deepseek-chat", "sk-test", "s", "u"))
            .isInstanceOf(DeepseekException.class)
            .hasMessageContaining("缺少 choices");
    }

    @Test
    void chatCompletion_throwsOnEmptyContent() {
        server.expect(requestTo(BASE_URL + "/v1/chat/completions"))
            .andRespond(withSuccess(
                "{\"choices\":[{\"message\":{\"content\":\"\"}}]}",
                MediaType.APPLICATION_JSON
            ));

        assertThatThrownBy(() -> chatClient.chatCompletion("deepseek-chat", "sk-test", "s", "u"))
            .isInstanceOf(DeepseekException.class)
            .hasMessageContaining("返回内容为空");
    }

    @Test
    void chatCompletion_throwsOnEmptyRawBody() {
        server.expect(requestTo(BASE_URL + "/v1/chat/completions"))
            .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> chatClient.chatCompletion("deepseek-chat", "sk-test", "s", "u"))
            .isInstanceOf(DeepseekException.class)
            .hasMessageContaining("返回空响应");
    }

    @Test
    void chatCompletion_throwsOnHttpError() {
        server.expect(requestTo(BASE_URL + "/v1/chat/completions"))
            .andRespond(withStatus(HttpStatus.BAD_GATEWAY).body("{\"error\":\"bad gateway\"}"));

        assertThatThrownBy(() -> chatClient.chatCompletion("deepseek-chat", "sk-test", "s", "u"))
            .isInstanceOf(DeepseekException.class)
            .hasMessageContaining("HTTP 502");
    }
}
