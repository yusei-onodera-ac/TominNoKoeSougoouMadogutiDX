package com.tominnokoe.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Slack通知チャネル（Incoming Webhook、JDK標準HttpClientのみで実装、追加依存なし）。
 * 環境変数 {@code SLACK_WEBHOOK_URL} で設定する。
 */
public final class SlackNotificationChannel implements NotificationChannel {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Override
    public boolean isConfigured() {
        String url = System.getenv("SLACK_WEBHOOK_URL");
        return url != null && !url.isBlank();
    }

    @Override
    public String channelName() {
        return "SLACK";
    }

    @Override
    public void send(NotificationMessage message) throws Exception {
        String webhookUrl = System.getenv("SLACK_WEBHOOK_URL");

        ObjectNode body = MAPPER.createObjectNode();
        body.put("text", message.subjectLine() + "\n" + message.bodyText());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(webhookUrl))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(body)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new RuntimeException("Slack Webhook呼び出しに失敗しました（HTTP " + response.statusCode() + "）: " + response.body());
        }
    }
}
