package com.tominnokoe.model.enums;

/** 案件の管轄分類。要件定義書1-4Cの出力スキーマ classification_type に対応。 */
public enum ClassificationType {
    TOKYO_METROPOLITAN("都管轄"),
    JURISDICTION_OTHER("他管轄（区市町村等）"),
    UNKNOWN("不明（要確認）");

    private final String label;

    ClassificationType(String label) {
        this.label = label;
    }

    /** 画面表示用の日本語ラベル（英語の分類名をそのまま出さないため）。 */
    public String getLabel() {
        return label;
    }
}
