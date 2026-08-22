package com.tominnokoe.classification.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Gemini API（generateContent）の薄いラッパー。JDK標準の{@link HttpClient}のみを使用し、
 * 追加の外部SDK依存を持たない。
 *
 * <p>環境変数 {@code GEMINI_API_KEY} が設定されている場合のみ利用可能。
 * {@code GEMINI_MODEL}（既定値 {@value #DEFAULT_MODEL}）でモデルを差し替えられる。
 * Google側のAPI仕様・モデル名は変更されうるため、失敗時は呼び出し元
 * （{@link com.tominnokoe.classification.ClassificationEngine}）でルールベースのモックへ
 * フォールバックする設計にしている。</p>
 */
public final class GeminiClient {

    private static final String DEFAULT_MODEL = "gemini-2.5-flash";
    private static final String API_BASE = "https://generativelanguage.googleapis.com/v1beta/models/";
    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .build();

    /** {@code GEMINI_API_KEY} 環境変数が設定されているかどうか。 */
    public static boolean isConfigured() {
        String key = System.getenv("GEMINI_API_KEY");
        return key != null && !key.isBlank();
    }

    /**
     * プロンプトを送信し、{@code responseSchema} で拘束されたJSON文字列を返す。
     * 失敗時は {@link RuntimeException} を投げる（呼び出し元でモックへフォールバックする想定）。
     */
    public String generateJson(String promptText) {
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY が設定されていません");
        }
        String model = System.getenv().getOrDefault("GEMINI_MODEL", DEFAULT_MODEL);

        ObjectNode requestBody = buildRequestBody(promptText);

        try {
            String uri = API_BASE + model + ":generateContent?key=" + apiKey;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .timeout(TIMEOUT)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(requestBody)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Gemini API呼び出しに失敗しました（HTTP " + response.statusCode() + "）: "
                        + truncate(response.body(), 500));
            }
            return extractText(response.body());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Gemini API呼び出し中にエラーが発生しました", e);
        }
    }

    private ObjectNode buildRequestBody(String promptText) {
        ObjectNode root = MAPPER.createObjectNode();

        ObjectNode part = MAPPER.createObjectNode();
        part.put("text", promptText);
        root.putArray("contents")
                .addObject()
                .putArray("parts")
                .add(part);

        ObjectNode generationConfig = root.putObject("generationConfig");
        generationConfig.put("responseMimeType", "application/json");
        generationConfig.set("responseSchema", GeminiResponseSchema.SCHEMA_NODE);
        generationConfig.put("temperature", 0.1);

        return root;
    }

    private String extractText(String responseBody) throws Exception {
        JsonNode root = MAPPER.readTree(responseBody);
        JsonNode candidates = root.path("candidates");
        if (!candidates.isArray() || candidates.isEmpty()) {
            throw new RuntimeException("Gemini APIレスポンスにcandidatesがありません: " + truncate(responseBody, 500));
        }
        JsonNode parts = candidates.get(0).path("content").path("parts");
        if (!parts.isArray() || parts.isEmpty()) {
            throw new RuntimeException("Gemini APIレスポンスにpartsがありません: " + truncate(responseBody, 500));
        }
        String text = parts.get(0).path("text").asText(null);
        if (text == null || text.isBlank()) {
            throw new RuntimeException("Gemini APIレスポンスのtextが空です: " + truncate(responseBody, 500));
        }
        return text;
    }

    private String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() > max ? s.substring(0, max) + "..." : s;
    }
}
