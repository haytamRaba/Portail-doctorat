package com.doctorat.suividoctorat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/react/**")
                .addResourceLocations("file:frontend/build/");

        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}