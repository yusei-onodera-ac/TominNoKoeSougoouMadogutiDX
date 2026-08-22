package com.tominnokoe.model.enums;

/**
 * 都民の声の受付チャネル。東京都公式サイトに掲載されている受付フロー図の6チャネルに対応。
 * {@code WEB_FORM} のみ都民本人が直接入力する完全自動フロー（将来LINEもここに合流予定）。
 * それ以外は電話・FAX・窓口来訪・手紙・意見箱で受け取った内容を、行政職員が
 * 統一代筆入力ページ（{@code /admin/manual-intake}）から代筆で入力する半自動フロー。
 * どちらの経路も同じ判定エンジン（{@link com.tominnokoe.classification.ClassificationEngine}）で処理される。
 */
public enum IntakeChannel {
    WEB_FORM,
    PHONE,
    FAX,
    VISIT,
    LETTER,
    OPINION_BOX
}
