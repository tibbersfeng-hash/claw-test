package com.openclaw.test.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;

/**
 * 请求包装过滤器
 * 包装请求以支持多次读取 body
 */
@Component
public class RequestWrapperFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;

            // 只包装 POST/PUT/PATCH 请求
            if ("POST".equalsIgnoreCase(httpRequest.getMethod()) ||
                "PUT".equalsIgnoreCase(httpRequest.getMethod()) ||
                "PATCH".equalsIgnoreCase(httpRequest.getMethod())) {

                ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(httpRequest);
                chain.doFilter(wrappedRequest, response);
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
