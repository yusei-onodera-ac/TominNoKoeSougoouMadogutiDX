package com.tominnokoe.notification;

/** 通知配信チャネル（メール・Slack等）の共通インターフェース。 */
public interface NotificationChannel {

    /** この環境変数等の設定でチャネルが利用可能かどうか。 */
    boolean isConfigured();

    /** 通知を送信する。失敗時は例外を投げる（呼び出し元で個別にログして継続する想定）。 */
    void send(NotificationMessage message) throws Exception;

    String channelName();
}
