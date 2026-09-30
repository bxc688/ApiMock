package com.apimock.config;

import com.apimock.interceptor.MockRequestInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MockInterceptorConfig implements WebMvcConfigurer {

    private final MockRequestInterceptor mockRequestInterceptor;

    public MockInterceptorConfig(MockRequestInterceptor mockRequestInterceptor) {
        this.mockRequestInterceptor = mockRequestInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(mockRequestInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/api/admin/**",
                        "/api/auth/**",
                        "/",
                        "/index.html",
                        "/css/**",
                        "/js/**",
                        "/favicon.ico",
                        "/static/**",
                        "/error"
                );
    }
}
