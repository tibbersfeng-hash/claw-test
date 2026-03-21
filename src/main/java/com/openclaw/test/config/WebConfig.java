package com.openclaw.test.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final AgentAuthInterceptor agentAuthInterceptor;

    public WebConfig(AuthInterceptor authInterceptor, AgentAuthInterceptor agentAuthInterceptor) {
        this.authInterceptor = authInterceptor;
        this.agentAuthInterceptor = agentAuthInterceptor;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // API Key 认证拦截器
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/tasks/**", "/api/requirements/**", "/api/agent/sign", "/api/agent/me")
                .excludePathPatterns("/api/identities/**");

        // Agent 签名认证拦截器（用于 Agent 间通信）
        registry.addInterceptor(agentAuthInterceptor)
                .addPathPatterns("/api/agent/message");
    }
}
