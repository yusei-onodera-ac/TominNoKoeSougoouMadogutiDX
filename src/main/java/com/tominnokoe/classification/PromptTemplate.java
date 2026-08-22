package com.tominnokoe.classification;

/**
 * 将来、本物のLLM API（Gemini API等を想定）へ差し替える際に使用する
 * システムプロンプトのドキュメント。現時点では実行されない（{@link ClassificationEngine}は
 * ルールベースのモックで動作している）。
 *
 * 変数マッピング（{@link com.tominnokoe.model.vo.ClassificationInput} /
 * {@link com.tominnokoe.model.vo.RetrievedContext} → プロンプト変数）:
 * <pre>
 *   {{USER_SELECTED_CATEGORY}}   ← ClassificationInput#getCategory()
 *   {{USER_SUBMITTED_SUBJECT}}   ← ClassificationInput#getSubject()
 *   {{USER_SUBMITTED_BODY}}      ← ClassificationInput#getBody()
 *   {{RAG_OFFICIAL_RULES}}       ← RetrievedContext#getMatchedOrgRules()
 *   {{RAG_SIMILAR_CASES}}        ← RetrievedContext#getSimilarCases()
 *   {{RAG_MATCHED_ENTITIES}}     ← RetrievedContext#getMatchedFacility() / getMatchedRoad()
 *   {{RAG_MUNICIPALITY_INFOS}}   ← RetrievedContext#getMatchedMunicipality()
 *   {{CONFIDENCE_THRESHOLD_HIGH}} ← ConfidenceScorer#HIGH_THRESHOLD (0.85)
 *   {{CONFIDENCE_THRESHOLD_LOW}}  ← ConfidenceScorer#LOW_THRESHOLD (0.70)
 * </pre>
 *
 * 実LLMへの差し替え手順: {@link ClassificationEngine#classify} の内部実装を、
 * 下記プロンプトへ上記マッピングで変数を埋め込んだ上でLLM APIを呼び出し、
 * レスポンスJSONを {@link com.tominnokoe.model.vo.ClassificationResult} にパースする処理へ置き換える。
 * 呼び出し元（Servlet群）は一切変更不要。
 */
public final class PromptTemplate {

    private PromptTemplate() {
    }

    public static final String SYSTEM_PROMPT = """
            # Role Definition
            あなたは東京都庁の広聴業務を支援する「都民の声 高精度ルーティング・トリアージエンジン」です。
            提供されたシステム変数およびオープンデータコンテキストのみを根拠として判定を行い、指定のJSONスキーマで出力してください。

            # System Constraints & Policies
            1. ハルシネーション（組織名の捏造）の完全禁止:
               担当組織名は必ず {{RAG_OFFICIAL_RULES}} に存在する公式部署名のみを使用してください。
            2. 安全・規約チェック:
               {{SAFETY_POLICY_RULES}} に照らし、誹謗中傷・営利目的等の違反がある場合は即座に is_inappropriate = true と判定してください。
            3. ユーザー申告ジャンルの活用:
               都民が申告したジャンル {{USER_SELECTED_CATEGORY}} を事前ヒント（Prior）として考慮しつつ、
               本文 {{USER_SUBMITTED_BODY}} との整合性を検証してください。
            4. 確信度閾値による決定論的ルーティング:
               confidence_score >= {{CONFIDENCE_THRESHOLD_HIGH}} の場合のみ、局・部・課の自動割り振りを確定してください。
               それ未満、または管轄の絞り込みが困難な場合は、無理に推測せず primary_bureau を "UNKNOWN" としてください。
            5. 既存行政フローの遵守（都外管轄の案内）:
               都の管轄外（区市町村・国）と判定された場合でも除外せず、{{RAG_MUNICIPALITY_INFOS}} を参照して
               都民への丁寧な案内文を生成してください。
            """;
}
