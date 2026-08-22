package com.tominnokoe.classification;

import com.tominnokoe.model.entity.MunicipalityEntity;

/**
 * 区市町村案内状・回答文ドラフト生成（F-A04）の文面バリエーション。
 * 「再生成」操作で複数の言い回しを提示できるよう、簡易的に2パターンを用意する。
 */
public final class GuidanceTemplates {

    private GuidanceTemplates() {
    }

    public static String render(MunicipalityEntity municipality, int variantIndex) {
        if (municipality == null) {
            return "本件は東京都の直接管轄ではない可能性があります。お手数ですが、関連する市区町村の窓口へご相談ください。";
        }
        if (variantIndex % 2 == 0) {
            return String.format(
                    "本件は「%s」の管轄事項に該当するため、東京都では直接対応いたしかねます。"
                            + "お手数をおかけしますが、%s（%s）へご相談ください。",
                    municipality.getLocalGovName(), municipality.getConsultationDesk(), municipality.getContactUrl());
        }
        return String.format(
                "この度はご意見をお寄せいただきありがとうございます。"
                        + "恐れ入りますが、ご指摘の内容は%sが所管する事項となっております。"
                        + "詳しくは%sまでお問い合わせいただけますと幸いです（%s）。",
                municipality.getLocalGovName(), municipality.getConsultationDesk(), municipality.getContactUrl());
    }
}
