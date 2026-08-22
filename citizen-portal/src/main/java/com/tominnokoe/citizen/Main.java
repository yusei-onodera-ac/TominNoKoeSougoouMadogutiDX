package com.tominnokoe.citizen;

import com.tominnokoe.dao.SeedData;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

import java.io.File;

/**
 * 都民向けポータルのエントリポイント。行政向け管理画面（admin-portal）とは完全に別の
 * 組み込みTomcat・別ポートで動作する独立アプリケーション（データベースのみ共有する）。
 *
 * 実行方法: mvn -pl citizen-portal exec:java
 */
public class Main {

    public static final int PORT = 8080;

    public static void main(String[] args) throws Exception {
        String webappDirLocation = resolveWebappDir();

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(PORT);
        tomcat.setBaseDir(createTempDir());
        tomcat.getConnector(); // HTTPコネクタを明示的に生成・登録する（呼ばないとポート待受が始まらない）

        Context ctx = tomcat.addWebapp("", new File(webappDirLocation).getAbsolutePath());
        // mvn exec:java 実行時、Tomcatのデフォルトの親クラスローダ解決は本プロジェクトの
        // 依存関係（tomcat-embed-jasper等）を含まないシステムクラスローダに落ちてしまうことがある。
        // Main自身を読み込んだクラスローダ（依存関係を正しく含む）を明示的に親として指定する。
        ctx.setParentClassLoader(Main.class.getClassLoader());
        // citizen-portal(8080)とadmin-portal(8081)は同じホスト名(localhost)の別ポートで動く別アプリのため、
        // 既定のセッションCookie名（JSESSIONID）のままだとブラウザ側でCookieが衝突し
        // （Cookieはポートではなくホストでスコープされるためlocalhost上の全ポートで共有されてしまう）、
        // 片方にアクセスするともう片方のセッションが上書きされてCSRFトークン不一致等の原因になる。
        // アプリごとに異なるセッションCookie名を明示的に設定して衝突を避ける。
        ctx.setSessionCookieName("CITIZEN_SESSIONID");
        ServletRegistrar.registerAll(tomcat, ctx);
        SeedData.seedIfEmpty(); // DBが空の場合のみデモ用ケースを投入（初回起動でも管理画面が空にならないように）

        System.out.println("=================================================");
        System.out.println(" 都民の声プラットフォーム - 都民向けポータル（独立アプリ）");
        System.out.println(" http://localhost:" + PORT + "/");
        System.out.println("=================================================");

        tomcat.start();
        tomcat.getServer().await();
    }

    private static String resolveWebappDir() throws Exception {
        // exec-maven-plugin の java ゴールは（-pl でモジュール指定していても）
        // カレントディレクトリを mvn 実行時のディレクトリのまま変更しないため、
        // cwd相対では解決できないことがある。クラスパス上のこのクラス自身の位置
        // （<module>/target/classes）から逆算してモジュールディレクトリを特定する。
        File classesDir = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        File moduleDir = classesDir.getParentFile().getParentFile(); // target/classes -> target -> <module>
        File webapp = new File(moduleDir, "src/main/webapp");
        if (webapp.exists()) {
            return webapp.getAbsolutePath();
        }
        File cwdRelative = new File("src/main/webapp");
        if (cwdRelative.exists()) {
            return cwdRelative.getAbsolutePath();
        }
        throw new IllegalStateException("webappディレクトリが見つかりません: " + webapp.getAbsolutePath());
    }

    private static String createTempDir() {
        try {
            File tempDir = File.createTempFile("tominnokoe-tomcat", "");
            tempDir.delete();
            tempDir.mkdir();
            tempDir.deleteOnExit();
            return tempDir.getAbsolutePath();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
