package com.tominnokoe.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tominnokoe.model.entity.FacilityEntity;
import com.tominnokoe.model.entity.MunicipalityEntity;
import com.tominnokoe.model.entity.OrgRuleEntity;
import com.tominnokoe.model.entity.PastCaseEntity;
import com.tominnokoe.model.entity.RoadEntity;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
 * 5大オープンデータ（参照専用・静的データ）を起動時にメモリへロードするローダー。
 * クラスパス上の {@code /data/*.json}（= {@code src/main/resources/data/}）から読み込む。
 * ロードは起動時に一度だけ行い、以降はイミュータブルなリストとして保持する。
 */
public final class DatasetLoader {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static volatile List<OrgRuleEntity> orgRules;
    private static volatile List<FacilityEntity> facilities;
    private static volatile List<RoadEntity> roads;
    private static volatile List<MunicipalityEntity> municipalities;
    private static volatile List<PastCaseEntity> pastCases;

    private DatasetLoader() {
    }

    /** アプリ起動時に一度だけ呼び出し、全データセットをロードする。 */
    public static synchronized void loadAll() {
        orgRules = load("/data/org_jurisdiction_rules.json", OrgRuleEntity[].class);
        facilities = load("/data/tokyo_facilities.json", FacilityEntity[].class);
        roads = load("/data/tokyo_roads.json", RoadEntity[].class);
        municipalities = load("/data/municipalities.json", MunicipalityEntity[].class);
        pastCases = load("/data/opinions_past_cases.json", PastCaseEntity[].class);
    }

    public static List<OrgRuleEntity> orgRules() {
        ensureLoaded();
        return orgRules;
    }

    public static List<FacilityEntity> facilities() {
        ensureLoaded();
        return facilities;
    }

    public static List<RoadEntity> roads() {
        ensureLoaded();
        return roads;
    }

    public static List<MunicipalityEntity> municipalities() {
        ensureLoaded();
        return municipalities;
    }

    public static List<PastCaseEntity> pastCases() {
        ensureLoaded();
        return pastCases;
    }

    private static void ensureLoaded() {
        if (orgRules == null) {
            loadAll();
        }
    }

    private static <T> List<T> load(String classpathResource, Class<T[]> arrayType) {
        try (InputStream in = DatasetLoader.class.getResourceAsStream(classpathResource)) {
            if (in == null) {
                throw new IllegalStateException("オープンデータが見つかりません: " + classpathResource);
            }
            T[] array = MAPPER.readValue(in, arrayType);
            return Collections.unmodifiableList(java.util.Arrays.asList(array));
        } catch (IOException e) {
            throw new RuntimeException("オープンデータの読み込みに失敗しました: " + classpathResource, e);
        }
    }
}
