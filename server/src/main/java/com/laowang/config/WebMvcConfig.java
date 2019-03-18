package com.laowang.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;


@Configuration
@Slf4j
public class WebMvcConfig extends WebMvcConfigurerAdapter {

    @Autowired
    private MyInterceptor myInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(myInterceptor).addPathPatterns("/**")
//                .excludePathPatterns("/login")
//                .excludePathPatterns("/error")
//                .excludePathPatterns("/index")
//                .excludePathPatterns("/file")
//                .excludePathPatterns("/static")
//                .excludePathPatterns("/signin")
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/","classpath:/template/","classpath:/public/");
        super.addResourceHandlers(registry);
    }
}
