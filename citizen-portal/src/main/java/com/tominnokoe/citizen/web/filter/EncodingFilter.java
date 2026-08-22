package com.tominnokoe.citizen.web.filter;

import jakarta.servlet.*;
import java.io.IOException;

/** 全リクエスト/レスポンスをUTF-8に固定する（日本語データを扱うため必須）。 */
public class EncodingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        chain.doFilter(request, response);
    }
}
