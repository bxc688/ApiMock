package com.apimock.config;

import com.apimock.interceptor.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AuthInterceptorConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    public AuthInterceptorConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/admin/**")
                .excludePathPatterns(
                        "/api/auth/**",
                        "/",
                        "/index.html",
                        "/css/**",
                        "/js/**",
                        "/favicon.ico",
                        "/static/**"
                );
    }
}
