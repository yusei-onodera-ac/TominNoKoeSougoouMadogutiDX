package com.tominnokoe.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tominnokoe.model.entity.CaseEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 案件（{@link CaseEntity}）の読み書きを担うリポジトリ。
 * 本番想定のデータベース（H2、{@link Database}参照）へJDBC経由で永続化する。
 * エンティティ全体はJSONとして {@code payload_json} 列に保持しつつ、検索・絞り込みに
 * 使う主要な属性（status/is_inappropriate/classification_type/primary_bureau）は
 * 実列として保持しSQLインデックスで高速に絞り込めるようにしている
 * （簡易だが「本番想定のRDBMS」として振る舞う実用的な折衷案）。
 * 書き込みは {@code synchronized} で直列化し、単一コネクションでの競合を避ける。
 */
public final class CaseRepository {

    private static final CaseRepository INSTANCE = new CaseRepository();

    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    private final AtomicInteger sequence = new AtomicInteger(0);

    private CaseRepository() {
        initSequence();
    }

    public static CaseRepository getInstance() {
        return INSTANCE;
    }

    public synchronized String nextCaseId() {
        return String.format("C-2026-%04d", sequence.incrementAndGet());
    }

    public synchronized void add(CaseEntity entity) {
        upsert(entity, true);
    }

    public synchronized void update(CaseEntity entity) {
        upsert(entity, false);
    }

    public synchronized Optional<CaseEntity> findById(String id) {
        String sql = "SELECT payload_json FROM cases WHERE id = ?";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(deserialize(rs.getString(1)));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("案件の取得に失敗しました: " + id, e);
        }
    }

    public synchronized List<CaseEntity> findAll() {
        String sql = "SELECT payload_json FROM cases ORDER BY created_at DESC";
        List<CaseEntity> results = new ArrayList<>();
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                results.add(deserialize(rs.getString(1)));
            }
        } catch (SQLException e) {
            throw new RuntimeException("案件一覧の取得に失敗しました", e);
        }
        return results;
    }

    private void upsert(CaseEntity entity, boolean isInsert) {
        String payload = serialize(entity);
        var classification = entity.getClassification();
        String classificationType = classification == null || classification.getClassificationType() == null
                ? null : classification.getClassificationType().name();
        boolean isInappropriate = classification != null && classification.isInappropriate();
        String primaryBureau = entity.getAssignedBureauOverride() != null
                ? entity.getAssignedBureauOverride()
                : (classification != null && classification.getRouting() != null
                        ? classification.getRouting().getPrimaryBureau() : null);

        String sql = """
            MERGE INTO cases (id, created_at, status, is_inappropriate, classification_type, primary_bureau, payload_json)
            KEY (id) VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setString(1, entity.getId());
            ps.setTimestamp(2, Timestamp.from(entity.getCreatedAt() == null ? Instant.now() : entity.getCreatedAt()));
            ps.setString(3, entity.getStatus() == null ? "NEW" : entity.getStatus().name());
            ps.setBoolean(4, isInappropriate);
            ps.setString(5, classificationType);
            ps.setString(6, primaryBureau);
            ps.setString(7, payload);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException((isInsert ? "案件の登録" : "案件の更新") + "に失敗しました: " + entity.getId(), e);
        }
    }

    private void initSequence() {
        String sql = "SELECT id FROM cases";
        int max = 0;
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String id = rs.getString(1);
                if (id != null && id.startsWith("C-2026-")) {
                    try {
                        max = Math.max(max, Integer.parseInt(id.substring("C-2026-".length())));
                    } catch (NumberFormatException ignored) {
                        // フォーマット外のIDは連番採番に影響させない
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("案件連番の初期化に失敗しました", e);
        }
        sequence.set(max);
    }

    private String serialize(CaseEntity entity) {
        try {
            return MAPPER.writeValueAsString(entity);
        } catch (Exception e) {
            throw new RuntimeException("案件のシリアライズに失敗しました: " + entity.getId(), e);
        }
    }

    private CaseEntity deserialize(String json) {
        try {
            return MAPPER.readValue(json, CaseEntity.class);
        } catch (Exception e) {
            throw new RuntimeException("案件のデシリアライズに失敗しました", e);
        }
    }
}
