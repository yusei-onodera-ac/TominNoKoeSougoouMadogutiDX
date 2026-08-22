package com.tominnokoe.classification.rules;

import com.tominnokoe.model.enums.InappropriateReason;

import java.util.List;
import java.util.Locale;

/**
 * 誹謗中傷・営業スパム・脅迫の簡易キーワード検出（モックの安全・規約チェック）。
 * 優先度は THREAT &gt; DEFAMATION &gt; COMMERCIAL_SPAM の順で最初に該当したものを採用する。
 * 実運用では要件定義書 {@code SAFETY_POLICY_RULES} に基づくより精緻な判定（実LLM）に置き換える。
 */
public final class InappropriateDetector {

    private static final List<String> THREAT_KEYWORDS = List.of(
            "殺す", "殺害", "爆破", "危害を加える", "火をつける", "殺してやる"
    );

    private static final List<String> DEFAMATION_KEYWORDS = List.of(
            "死ね", "バカ野郎", "馬鹿野郎", "無能", "クズ", "消えろ", "ゴミ以下"
    );

    private static final List<String> COMMERCIAL_SPAM_KEYWORDS = List.of(
            "副業", "今すぐ登録", "儲かる", "割引コード", "限定オファー", "ご案内キャンペーン", "アフィリエイト"
    );

    public InappropriateReason detect(String text) {
        String normalized = text == null ? "" : text.toLowerCase(Locale.JAPAN);
        if (containsAny(normalized, THREAT_KEYWORDS)) {
            return InappropriateReason.THREAT;
        }
        if (containsAny(normalized, DEFAMATION_KEYWORDS)) {
            return InappropriateReason.DEFAMATION;
        }
        if (containsAny(normalized, COMMERCIAL_SPAM_KEYWORDS)) {
            return InappropriateReason.COMMERCIAL_SPAM;
        }
        return InappropriateReason.NONE;
    }

    private boolean containsAny(String text, List<String> keywords) {
        for (String k : keywords) {
            if (text.contains(k.toLowerCase(Locale.JAPAN))) {
                return true;
            }
        }
        return false;
    }
}
