package com.tominnokoe;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

import java.io.File;

/**
 * アプリケーションのエントリポイント。組み込みTomcatを起動し、
 * このクラスの中でサーブレット・フィルタをプログラム的に登録する
 * （アノテーションスキャンには依存しない — セットアップの不確実性を減らすため）。
 *
 * 実行方法: mvn exec:java
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
        ServletRegistrar.registerAll(tomcat, ctx);

        System.out.println("=================================================");
        System.out.println(" 都民の声 次世代ハイブリッド仕分けプラットフォーム（プロトタイプ）");
        System.out.println(" http://localhost:" + PORT + "/");
        System.out.println("=================================================");

        tomcat.start();
        tomcat.getServer().await();
    }

    private static String resolveWebappDir() {
        // mvn exec:java 実行時のカレントディレクトリはプロジェクトルート想定
        File webapp = new File("src/main/webapp");
        if (webapp.exists()) {
            return webapp.getPath();
        }
        throw new IllegalStateException(
                "webappディレクトリが見つかりません。プロジェクトルートで実行してください: " + webapp.getAbsolutePath());
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
