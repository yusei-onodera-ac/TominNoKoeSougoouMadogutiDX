package com.tominnokoe.notification;

import java.util.ArrayList;
import java.util.List;

/**
 * 設定されている通知チャネル（メール・Slack）すべてへ配信を試みる。
 * 1つのチャネルの失敗が他のチャネルや呼び出し元の処理を止めないよう、個別にcatchする。
 * どのチャネルも設定されていない場合は何も送信せず、その旨を結果に含める
 * （デモ環境で通知配信の資格情報が無くてもアプリが壊れないようにするための設計）。
 */
public final class NotificationDispatcher {

    private static final NotificationDispatcher INSTANCE = new NotificationDispatcher(
            List.of(new EmailNotificationChannel(), new SlackNotificationChannel()));

    private final List<NotificationChannel> channels;

    NotificationDispatcher(List<NotificationChannel> channels) {
        this.channels = channels;
    }

    public static NotificationDispatcher getInstance() {
        return INSTANCE;
    }

    /** @return 各チャネルの送信結果（監査ログ用の人間可読な文字列） */
    public List<String> dispatch(NotificationMessage message) {
        List<String> results = new ArrayList<>();
        boolean anyConfigured = false;

        for (NotificationChannel channel : channels) {
            if (!channel.isConfigured()) {
                continue;
            }
            anyConfigured = true;
            try {
                channel.send(message);
                results.add(channel.channelName() + ": 送信成功");
            } catch (Exception e) {
                results.add(channel.channelName() + ": 送信失敗（" + e.getMessage() + "）");
            }
        }

        if (!anyConfigured) {
            results.add("通知チャネル未設定のため送信をスキップしました（SMTP_HOST/SLACK_WEBHOOK_URL等を参照）");
        }
        return results;
    }
}
