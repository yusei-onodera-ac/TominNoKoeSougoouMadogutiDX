package com.tominnokoe.model.enums;

/** 規約違反理由。優先度は THREAT > DEFAMATION > COMMERCIAL_SPAM の順で判定する。 */
public enum InappropriateReason {
    NONE,
    DEFAMATION,
    COMMERCIAL_SPAM,
    THREAT,
    OUT_OF_SCOPE
}
