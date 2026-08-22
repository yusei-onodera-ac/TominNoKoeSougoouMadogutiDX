package com.tominnokoe.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 管理操作（アサイン、不適切復元、通知ステータス変更、エクスポート実行等）の監査ログ。
 * 誰が・いつ・何をしたかをデータベース（{@code audit_log}テーブル）に記録する
 * （F-A02「監査ログとして管理」要件、およびセキュリティ設計の一部）。
 */
public final class AuditLog {

    private static final AuditLog INSTANCE = new AuditLog();

    private AuditLog() {
    }

    public static AuditLog getInstance() {
        return INSTANCE;
    }

    public synchronized void record(String actor, String action, String targetCaseId, String details) {
        String sql = "INSERT INTO audit_log (ts, actor, action, target_case_id, details) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.from(Instant.now()));
            ps.setString(2, actor);
            ps.setString(3, action);
            ps.setString(4, targetCaseId);
            ps.setString(5, details);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("監査ログの書き込みに失敗しました", e);
        }
    }

    public synchronized List<Entry> findAll() {
        String sql = "SELECT ts, actor, action, target_case_id, details FROM audit_log ORDER BY ts DESC";
        List<Entry> entries = new ArrayList<>();
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                entries.add(new Entry(
                        rs.getTimestamp(1).toInstant(),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getString(5)));
            }
        } catch (SQLException e) {
            throw new RuntimeException("監査ログの取得に失敗しました", e);
        }
        return entries;
    }

    public static final class Entry {
        public final Instant timestamp;
        public final String actor;
        public final String action;
        public final String targetCaseId;
        public final String details;

        public Entry(Instant timestamp, String actor, String action, String targetCaseId, String details) {
            this.timestamp = timestamp;
            this.actor = actor;
            this.action = action;
            this.targetCaseId = targetCaseId;
            this.details = details;
        }
    }
}
