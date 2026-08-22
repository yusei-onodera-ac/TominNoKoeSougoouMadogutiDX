package com.tominnokoe.admin;

import com.tominnokoe.dao.SeedData;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

import java.io.File;

/**
 * 行政向け管理画面のエントリポイント。都民向けポータル（citizen-portal）とは完全に別の
 * 組み込みTomcat・別ポートで動作する独立アプリケーション（データベースのみ共有する）。
 *
 * 実行方法: mvn -pl admin-portal exec:java
 */
public class Main {

    public static final int PORT = 8081;

    public static void main(String[] args) throws Exception {
        String webappDirLocation = resolveWebappDir();

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(PORT);
        tomcat.setBaseDir(createTempDir());
        tomcat.getConnector(); // HTTPコネクタを明示的に生成・登録する（呼ばないとポート待受が始まらない）

        Context ctx = tomcat.addWebapp("", new File(webappDirLocation).getAbsolutePath());
        ctx.setParentClassLoader(Main.class.getClassLoader());
        // citizen-portal(8080)とadmin-portal(8081)は同じホスト名(localhost)の別ポートで動く別アプリのため、
        // 既定のセッションCookie名（JSESSIONID）のままだとブラウザ側でCookieが衝突する
        // （Cookieはポートではなくホストでスコープされるためlocalhost上の全ポートで共有されてしまう）。
        // アプリごとに異なるセッションCookie名を明示的に設定して衝突を避ける。
        ctx.setSessionCookieName("ADMIN_SESSIONID");
        ServletRegistrar.registerAll(tomcat, ctx);
        SeedData.seedIfEmpty(); // DBが空の場合のみデモ用ケースを投入（citizen-portalと独立に起動されても空にならないように）

        System.out.println("=================================================");
        System.out.println(" 都民の声プラットフォーム - 行政向け管理画面（独立アプリ）");
        System.out.println(" http://localhost:" + PORT + "/admin");
        System.out.println("=================================================");

        tomcat.start();
        tomcat.getServer().await();
    }

    private static String resolveWebappDir() throws Exception {
        // exec-maven-plugin の java ゴールは（-pl でモジュール指定していても）カレントディレクトリを
        // mvn実行時のディレクトリのまま変更しないため、クラスパス上のこのクラス自身の位置
        // （<module>/target/classes）から逆算してモジュールディレクトリを特定する。
        File classesDir = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        File moduleDir = classesDir.getParentFile().getParentFile();
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
            File tempDir = File.createTempFile("tominnokoe-admin-tomcat", "");
            tempDir.delete();
            tempDir.mkdir();
            tempDir.deleteOnExit();
            return tempDir.getAbsolutePath();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
