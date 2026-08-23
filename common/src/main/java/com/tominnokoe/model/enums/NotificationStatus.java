package com.tominnokoe.model.enums;

/** ガバナンス通知ツリーの各ノードの状態（F-A03の進捗管理で使用）。 */
public enum NotificationStatus {
    PENDING("未通知"),
    NOTIFIED("通知済み"),
    ACKED("確認済み"),
    DONE("対応完了");

    private final String label;

    NotificationStatus(String label) {
        this.label = label;
    }

    /** 画面表示用の日本語ラベル（英語のステータス名をそのまま出さないため）。 */
    public String getLabel() {
        return label;
    }
}
