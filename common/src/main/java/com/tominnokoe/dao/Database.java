package com.tominnokoe.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 本番想定のデータベース接続管理。
 * デフォルトはH2（ファイルベース、純Java、ネイティブ依存なし）の組み込みモードだが、
 * 環境変数 {@code DB_JDBC_URL} / {@code DB_USER} / {@code DB_PASSWORD} を設定すれば
 * PostgreSQL・MySQL等の本番RDBMSへそのまま接続できる（標準JDBC経由）。
 * その場合は対応するJDBCドライバをpom.xmlへ追加すること。
 *
 * 簡易実装のため、素朴な単一コネクション運用としている
 * （本番移行時はコネクションプール（HikariCP等）への置き換えを推奨）。
 */
public final class Database {

    private static final String DEFAULT_JDBC_URL = "jdbc:h2:./data/tominnokoe;AUTO_SERVER=TRUE";

    private static volatile Connection connection;

    private Database() {
    }

    public static synchronized Connection getConnection() {
        if (connection != null) {
            return connection;
        }
        String url = System.getenv().getOrDefault("DB_JDBC_URL", DEFAULT_JDBC_URL);
        String user = System.getenv().getOrDefault("DB_USER", "sa");
        String password = System.getenv().getOrDefault("DB_PASSWORD", "");
        try {
            connection = DriverManager.getConnection(url, user, password);
            initSchema(connection);
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException("データベースへの接続に失敗しました: " + url, e);
        }
    }

    private static void initSchema(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS cases (
                    id VARCHAR(32) PRIMARY KEY,
                    created_at TIMESTAMP NOT NULL,
                    status VARCHAR(32) NOT NULL,
                    is_inappropriate BOOLEAN NOT NULL,
                    classification_type VARCHAR(32),
                    primary_bureau VARCHAR(128),
                    payload_json CLOB NOT NULL
                )
                """);
            st.execute("CREATE INDEX IF NOT EXISTS idx_cases_status ON cases(status)");
            st.execute("CREATE INDEX IF NOT EXISTS idx_cases_bureau ON cases(primary_bureau)");
            st.execute("CREATE INDEX IF NOT EXISTS idx_cases_inappropriate ON cases(is_inappropriate)");

            st.execute("""
                CREATE TABLE IF NOT EXISTS audit_log (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    ts TIMESTAMP NOT NULL,
                    actor VARCHAR(128),
                    action VARCHAR(64) NOT NULL,
                    target_case_id VARCHAR(32),
                    details CLOB
                )
                """);
        }
    }
}
