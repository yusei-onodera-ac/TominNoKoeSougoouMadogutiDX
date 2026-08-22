package com.tominnokoe.dao;

import com.tominnokoe.classification.ClassificationEngine;
import com.tominnokoe.model.entity.CaseEntity;
import com.tominnokoe.model.vo.ClassificationInput;
import com.tominnokoe.model.vo.ClassificationResult;

import java.time.Instant;

/**
 * データベースが空の場合にのみ、5つのベンチマークカテゴリを網羅するデモ用ケースを投入する。
 * これにより初回起動直後でも管理画面が空にならず、デモの見せ方（README参照）をすぐに再現できる。
 */
public final class SeedData {

    private SeedData() {
    }

    public static void seedIfEmpty() {
        CaseRepository repo = CaseRepository.getInstance();
        if (!repo.findAll().isEmpty()) {
            return;
        }
        ClassificationEngine engine = new ClassificationEngine();

        seedOne(repo, engine, "道路・交通", "環八の街路樹について",
                "環八通りの街路樹の剪定をお願いしたい。伸びすぎて見通しが悪い。");
        seedOne(repo, engine, null, "近所の区立公園について",
                "近所の杉並区立の公園のブランコが破損している。直してほしい。");
        seedOne(repo, engine, "道路・交通", "駅前の道路陥没",
                "新宿区内の世田谷通り沿いの都道に大きな陥没があり危険です。");
        seedOne(repo, engine, null, "違法駐輪とバス増便",
                "駅前の違法駐輪対策と都営バスの増便をお願いしたい。");
        seedOne(repo, engine, "福祉", "特別養護老人ホームの件",
                "親を特別養護老人ホームに申し込んでいるが、待機期間が長く不安です。介護保険についても教えてほしい。");
        seedOne(repo, engine, null, "近所の区道の街灯について",
                "自宅前の千代田区道の街灯が切れていて夜間暗く危険です。");
        seedOne(repo, engine, null, "緑を増やしてほしい",
                "都内の緑をもっと増やしてほしいです。");
        seedOne(repo, engine, null, "苦情",
                "お前なんか死ねばいいのに、バカ野郎。");
    }

    private static void seedOne(CaseRepository repo, ClassificationEngine engine,
                                 String category, String subject, String body) {
        ClassificationInput input = new ClassificationInput(category, subject, body);
        ClassificationResult result = engine.classify(input, false);

        CaseEntity entity = new CaseEntity();
        entity.setId(repo.nextCaseId());
        entity.setCreatedAt(Instant.now());
        entity.setCategory(category);
        entity.setSubject(subject);
        entity.setBody(body);
        entity.setClassification(result);
        repo.add(entity);
    }
}
