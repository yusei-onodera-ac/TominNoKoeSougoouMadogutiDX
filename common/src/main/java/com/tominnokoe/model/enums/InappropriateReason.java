package com.tominnokoe.model.enums;

/** 規約違反理由。優先度は THREAT > DEFAMATION > COMMERCIAL_SPAM の順で判定する。 */
public enum InappropriateReason {
    NONE("該当なし"),
    DEFAMATION("誹謗中傷"),
    COMMERCIAL_SPAM("営業・スパム行為"),
    THREAT("脅迫的な表現"),
    OUT_OF_SCOPE("対象外の内容");

    private final String label;

    InappropriateReason(String label) {
        this.label = label;
    }

    /** 画面表示用の日本語ラベル（英語の理由名をそのまま出さないため）。 */
    public String getLabel() {
        return label;
    }
}
