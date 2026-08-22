package com.tominnokoe.classification.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Gemini APIの {@code generationConfig.responseSchema} に渡すJSONスキーマ。
 * 要件定義書1-4Cの出力スキーマ（JSON Schema, snake_case）をそのまま踏襲し、
 * Gemini側のスキーマ表現（{@code type}に大文字の型名を使う）へ変換したもの。
 */
final class GeminiResponseSchema {

    private GeminiResponseSchema() {
    }

    private static final String SCHEMA_JSON = """
            {
              "type": "OBJECT",
              "properties": {
                "is_inappropriate": { "type": "BOOLEAN" },
                "inappropriate_reason": {
                  "type": "STRING",
                  "enum": ["NONE", "DEFAMATION", "COMMERCIAL_SPAM", "THREAT", "OUT_OF_SCOPE"]
                },
                "classification_type": {
                  "type": "STRING",
                  "enum": ["TOKYO_METROPOLITAN", "JURISDICTION_OTHER", "UNKNOWN"]
                },
                "routing": {
                  "type": "OBJECT",
                  "properties": {
                    "primary_bureau": { "type": "STRING" },
                    "primary_division": { "type": "STRING" },
                    "primary_section": { "type": "STRING" },
                    "action_owner": { "type": "STRING" },
                    "governance_notification_tree": {
                      "type": "ARRAY",
                      "items": {
                        "type": "OBJECT",
                        "properties": {
                          "level": { "type": "STRING", "enum": ["HEAD_OFFICE", "BUREAU", "DIVISION", "SECTION_SITE"] },
                          "department_name": { "type": "STRING" },
                          "purpose": { "type": "STRING" }
                        },
                        "required": ["level", "department_name", "purpose"]
                      }
                    }
                  },
                  "required": ["primary_bureau"]
                },
                "external_guidance": {
                  "type": "OBJECT",
                  "properties": {
                    "target_entity": { "type": "STRING" },
                    "explanation_text": { "type": "STRING" },
                    "contact_url": { "type": "STRING" }
                  }
                },
                "confidence_score": { "type": "NUMBER" },
                "evidence_sources": { "type": "ARRAY", "items": { "type": "STRING" } }
              },
              "required": ["is_inappropriate", "classification_type", "confidence_score", "evidence_sources"]
            }
            """;

    static final JsonNode SCHEMA_NODE = parse();

    private static JsonNode parse() {
        try {
            return new ObjectMapper().readTree(SCHEMA_JSON);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
