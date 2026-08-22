package com.tominnokoe.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 管理操作（アサイン、不適切復元、通知ステータス変更、エクスポート実行等）の簡易監査ログ。
 * 誰が・いつ・何をしたかを {@code data/audit_log.json} に追記する
 * （F-A02「監査ログとして管理」要件、およびセキュリティ設計の一部）。
 */
public final class AuditLog {

    private static final AuditLog INSTANCE = new AuditLog();
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT);

    private final File file = new File("data/audit_log.json");
    private final List<Entry> entries = new ArrayList<>();

    private AuditLog() {
        load();
    }

    public static AuditLog getInstance() {
        return INSTANCE;
    }

    public synchronized void record(String actor, String action, String targetCaseId, String details) {
        entries.add(new Entry(Instant.now(), actor, action, targetCaseId, details));
        persist();
    }

    public synchronized List<Entry> findAll() {
        return new ArrayList<>(entries);
    }

    private void load() {
        if (!file.exists()) {
            return;
        }
        try {
            Entry[] loaded = MAPPER.readValue(file, Entry[].class);
            entries.addAll(java.util.Arrays.asList(loaded));
        } catch (IOException e) {
            // 監査ログの読み込み失敗はアプリ起動を止めない（空の状態から開始）
            System.err.println("監査ログの読み込みに失敗しました: " + e.getMessage());
        }
    }

    private void persist() {
        try {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            MAPPER.writeValue(file, entries);
        } catch (IOException e) {
            throw new RuntimeException("監査ログの書き込みに失敗しました", e);
        }
    }

    public static final class Entry {
        public Instant timestamp;
        public String actor;
        public String action;
        public String targetCaseId;
        public String details;

        public Entry() {
        }

        public Entry(Instant timestamp, String actor, String action, String targetCaseId, String details) {
            this.timestamp = timestamp;
            this.actor = actor;
            this.action = action;
            this.targetCaseId = targetCaseId;
            this.details = details;
        }
    }
}
