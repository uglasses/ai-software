package com.example.jobplatform.llm;

import com.example.jobplatform.config.DeepseekProperties;
import com.example.jobplatform.exception.DeepseekException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class DeepseekChatClient {

    private static final Logger log = LoggerFactory.getLogger(DeepseekChatClient.class);

    private final RestClient deepseekRestClient;
    private final ObjectMapper objectMapper;
    private final DeepseekProperties properties;

    public DeepseekChatClient(RestClient deepseekRestClient,
                              ObjectMapper objectMapper,
                              DeepseekProperties properties) {
        this.deepseekRestClient = deepseekRestClient;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    /**
     * Calls DeepSeek chat completions (OpenAI-compatible). Uses configured model when {@code model} is null.
     */
    public String chatCompletion(String model, String apiKey, String systemPrompt, String userPrompt) {
        String useModel = model != null && !model.isBlank() ? model : properties.getModel();
        Map<String, Object> body = Map.of(
            "model", useModel,
            "messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
            )
        );
        long startedAt = System.nanoTime();
        boolean success = false;
        try {
            String raw = deepseekRestClient.post()
                .uri("/v1/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);
            if (raw == null || raw.isBlank()) {
                throw new DeepseekException("DeepSeek 返回空响应");
            }
            JsonNode root = objectMapper.readTree(raw);
            JsonNode err = root.get("error");
            if (err != null && !err.isNull()) {
                String msg = err.path("message").asText("未知错误");
                throw new DeepseekException("DeepSeek API 错误: " + msg);
            }
            JsonNode choices = root.get("choices");
            if (choices == null || !choices.isArray() || choices.isEmpty()) {
                throw new DeepseekException("DeepSeek 响应缺少 choices");
            }
            String content = choices.get(0).path("message").path("content").asText(null);
            if (content == null || content.isBlank()) {
                throw new DeepseekException("DeepSeek 返回内容为空");
            }
            success = true;
            return content.trim();
        } catch (RestClientResponseException e) {
            String hint = e.getResponseBodyAsString();
            if (hint != null && hint.length() > 500) {
                hint = hint.substring(0, 500) + "...";
            }
            throw new DeepseekException("调用 DeepSeek 失败 HTTP " + e.getStatusCode().value()
                + (hint == null || hint.isBlank() ? "" : ": " + hint), e);
        } catch (RestClientException e) {
            throw new DeepseekException("调用 DeepSeek 网络异常: " + e.getMessage(), e);
        } catch (DeepseekException e) {
            throw e;
        } catch (Exception e) {
            throw new DeepseekException("解析 DeepSeek 响应失败: " + e.getMessage(), e);
        } finally {
            long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000L;
            log.info("[DeepSeek] chatCompletion {}，耗时: {} ms，model={}",
                success ? "成功" : "失败", elapsedMs, useModel);
        }
    }
}
