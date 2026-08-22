package com.tominnokoe.notification;

import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

/**
 * メール通知チャネル（Jakarta Mail / Angus Mail実装、純Java）。
 * 環境変数 {@code SMTP_HOST}（必須）・{@code SMTP_PORT}（既定587）・
 * {@code SMTP_USERNAME}・{@code SMTP_PASSWORD}・{@code MAIL_FROM}（必須）・
 * {@code SMTP_STARTTLS}（既定true）で設定する。
 */
public final class EmailNotificationChannel implements NotificationChannel {

    @Override
    public boolean isConfigured() {
        String host = System.getenv("SMTP_HOST");
        String from = System.getenv("MAIL_FROM");
        return host != null && !host.isBlank() && from != null && !from.isBlank();
    }

    @Override
    public String channelName() {
        return "EMAIL";
    }

    @Override
    public void send(NotificationMessage message) throws Exception {
        if (message.getRecipientEmail() == null || message.getRecipientEmail().isBlank()) {
            throw new IllegalArgumentException("宛先メールアドレスが設定されていません（事務分掌データのcontactEmail未設定）");
        }

        String host = System.getenv("SMTP_HOST");
        String port = System.getenv().getOrDefault("SMTP_PORT", "587");
        String username = System.getenv("SMTP_USERNAME");
        String password = System.getenv("SMTP_PASSWORD");
        String from = System.getenv("MAIL_FROM");
        boolean startTls = !"false".equalsIgnoreCase(System.getenv().getOrDefault("SMTP_STARTTLS", "true"));

        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", username != null && !username.isBlank() ? "true" : "false");
        props.put("mail.smtp.starttls.enable", String.valueOf(startTls));

        Session session = (username != null && !username.isBlank())
                ? Session.getInstance(props, new jakarta.mail.Authenticator() {
                    @Override
                    protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
                        return new jakarta.mail.PasswordAuthentication(username, password);
                    }
                })
                : Session.getInstance(props);

        MimeMessage mime = new MimeMessage(session);
        mime.setFrom(new InternetAddress(from));
        mime.setRecipients(Message.RecipientType.TO, InternetAddress.parse(message.getRecipientEmail()));
        mime.setSubject(message.subjectLine(), "UTF-8");
        mime.setText(message.bodyText(), "UTF-8");

        Transport.send(mime);
    }
}
