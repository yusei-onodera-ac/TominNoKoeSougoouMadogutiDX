package com.tominnokoe.admin.web.filter;

import com.tominnokoe.admin.security.IpAllowlist;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * LGWAN（総合行政ネットワーク）本番閉域網相当のアクセス制限（ベストエフォート・疑似実装）。
 *
 * <p>実際のLGWANは専用線・行政共通のセキュアネットワークであり、この環境（インターネット上の
 * デモ環境）から本物のLGWAN接続を再現することはできない。その代替として、環境変数
 * {@code ADMIN_IP_ALLOWLIST}（カンマ区切りのIPアドレス・CIDR）が設定されている場合のみ、
 * 管理画面全体へのアクセスを許可されたネットワークからのみに制限する「近似」を行う。
 * 未設定時（既定）はローカルデモの利便性を優先しアクセス制限を行わない。</p>
 *
 * <p>本番でLGWAN接続そのものを行う場合は、この疑似フィルタではなく、実際のLGWAN回線・
 * 行政共通のリバースプロキシ／WAF層でのネットワーク制御に置き換えることが前提となる
 * （改訂版要件定義書に明記）。</p>
 */
public class LgwanSimulationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        List<String> allowlist = IpAllowlist.parseEnv(System.getenv("ADMIN_IP_ALLOWLIST"));
        String remoteAddr = request.getRemoteAddr();

        if (!IpAllowlist.isAllowed(remoteAddr, allowlist)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "このシステムは許可されたネットワーク（LGWAN相当）からのみアクセスできます。");
            return;
        }
        chain.doFilter(req, res);
    }
}
