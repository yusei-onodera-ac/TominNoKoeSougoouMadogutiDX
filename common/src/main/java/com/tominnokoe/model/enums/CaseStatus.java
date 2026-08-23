package com.tominnokoe.model.enums;

/** 案件の処理ステータス（管理画面での進捗管理用）。 */
public enum CaseStatus {
    NEW("新規"),
    TRIAGED("仕分け済み"),
    ASSIGNED("アサイン済み"),
    RESOLVED("対応済み");

    private final String label;

    CaseStatus(String label) {
        this.label = label;
    }

    /** 画面表示用の日本語ラベル（英語のステータス名をそのまま出さないため）。 */
    public String getLabel() {
        return label;
    }
}
