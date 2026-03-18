package com.openclaw.test.config;

import com.openclaw.test.entity.Identity;
import com.openclaw.test.service.IdentityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String IDENTITY_ATTRIBUTE = "identity";
    public static final String AUTH_HEADER = "X-API-Key";

    private final IdentityService identityService;

    public AuthInterceptor(IdentityService identityService) {
        this.identityService = identityService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行 OPTIONS 请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // GET /api/tasks 不需要认证
        if ("GET".equalsIgnoreCase(request.getMethod()) && "/api/tasks".equals(request.getRequestURI())) {
            return true;
        }

        String apiKey = request.getHeader(AUTH_HEADER);

        if (apiKey == null || apiKey.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"status\":401,\"message\":\"缺少认证头 X-API-Key\"}");
            return false;
        }

        try {
            Identity identity = identityService.getIdentityByApiKey(apiKey);
            request.setAttribute(IDENTITY_ATTRIBUTE, identity);
            return true;
        } catch (RuntimeException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"status\":401,\"message\":\"无效的 API Key\"}");
            return false;
        }
    }
}
