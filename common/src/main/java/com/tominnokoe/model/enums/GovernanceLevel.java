package com.tominnokoe.model.enums;

/** ガバナンス通知チェーンにおける階層レベル。 */
public enum GovernanceLevel {
    HEAD_OFFICE("総合窓口"),
    BUREAU("局"),
    DIVISION("部"),
    SECTION_SITE("現場出先機関");

    private final String label;

    GovernanceLevel(String label) {
        this.label = label;
    }

    /** 画面表示用の日本語ラベル（英語の階層名をそのまま出さないため）。 */
    public String getLabel() {
        return label;
    }
}
