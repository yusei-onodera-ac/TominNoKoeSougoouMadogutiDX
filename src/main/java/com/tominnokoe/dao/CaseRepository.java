package com.tominnokoe.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tominnokoe.model.entity.CaseEntity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 案件（{@link CaseEntity}）の読み書きを担う簡易リポジトリ。
 * 永続化はネイティブ依存のあるDBを避け、{@code data/cases.json} へのJSONファイル書き込みで行う
 * （メモリ内リストを正としてキャッシュし、書き込みのたびにファイルへ同期する）。
 * 書き込みは {@code synchronized} で直列化し、単純な競合を避ける。
 *
 * 本番移行時はこのクラスの内部実装だけを差し替えれば良い（呼び出し側のシグネチャは維持）。
 */
public final class CaseRepository {

    private static final CaseRepository INSTANCE = new CaseRepository();

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT);

    private final File storeFile;
    private final List<CaseEntity> cases = new ArrayList<>();
    private final AtomicInteger sequence = new AtomicInteger(0);

    private CaseRepository() {
        this.storeFile = new File("data/cases.json");
        load();
    }

    public static CaseRepository getInstance() {
        return INSTANCE;
    }

    public synchronized String nextCaseId() {
        int n = sequence.incrementAndGet();
        return String.format("C-2026-%04d", n);
    }

    public synchronized void add(CaseEntity entity) {
        cases.add(entity);
        persist();
    }

    public synchronized Optional<CaseEntity> findById(String id) {
        return cases.stream().filter(c -> c.getId().equals(id)).findFirst();
    }

    public synchronized List<CaseEntity> findAll() {
        List<CaseEntity> copy = new ArrayList<>(cases);
        copy.sort(Comparator.comparing(CaseEntity::getCreatedAt).reversed());
        return copy;
    }

    public synchronized void update(CaseEntity entity) {
        for (int i = 0; i < cases.size(); i++) {
            if (cases.get(i).getId().equals(entity.getId())) {
                cases.set(i, entity);
                persist();
                return;
            }
        }
        throw new IllegalArgumentException("案件が見つかりません: " + entity.getId());
    }

    private void load() {
        if (!storeFile.exists()) {
            return;
        }
        try {
            CaseEntity[] loaded = MAPPER.readValue(storeFile, CaseEntity[].class);
            cases.addAll(java.util.Arrays.asList(loaded));
            int maxSeq = 0;
            for (CaseEntity c : cases) {
                String id = c.getId();
                if (id != null && id.startsWith("C-2026-")) {
                    try {
                        maxSeq = Math.max(maxSeq, Integer.parseInt(id.substring("C-2026-".length())));
                    } catch (NumberFormatException ignored) {
                        // フォーマット外のID（デモ投入データ等）は連番採番に影響させない
                    }
                }
            }
            sequence.set(maxSeq);
        } catch (IOException e) {
            throw new RuntimeException("案件データの読み込みに失敗しました: " + storeFile.getAbsolutePath(), e);
        }
    }

    private void persist() {
        try {
            File parent = storeFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            MAPPER.writeValue(storeFile, cases);
        } catch (IOException e) {
            throw new RuntimeException("案件データの書き込みに失敗しました: " + storeFile.getAbsolutePath(), e);
        }
    }
}
