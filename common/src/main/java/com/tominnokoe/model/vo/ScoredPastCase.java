package com.tominnokoe.model.vo;

import com.tominnokoe.model.entity.PastCaseEntity;

/** 類似度スコア付きで検索された過去の都民の声事例（RAG類似事例検索の結果）。 */
public final class ScoredPastCase {
    private final PastCaseEntity pastCase;
    private final double score;

    public ScoredPastCase(PastCaseEntity pastCase, double score) {
        this.pastCase = pastCase;
        this.score = score;
    }

    public PastCaseEntity getPastCase() { return pastCase; }
    public double getScore() { return score; }
}
