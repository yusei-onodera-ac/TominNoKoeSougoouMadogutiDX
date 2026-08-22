package com.tominnokoe.model.enums;

/** ガバナンス通知ツリーの各ノードの状態（F-A03の進捗管理で使用）。 */
public enum NotificationStatus {
    PENDING,
    NOTIFIED,
    ACKED,
    DONE
}
