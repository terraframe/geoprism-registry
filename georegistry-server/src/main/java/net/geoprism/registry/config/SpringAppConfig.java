/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.config;

import java.util.List;
import java.util.concurrent.Executor;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.MultipartConfigElement;
import net.geoprism.EncodingFilter;
import net.geoprism.registry.GeoregistryProperties;
import net.geoprism.spring.web.JsonExceptionHandler;

@Configuration
@ComponentScan(basePackages = { //
    "net.geoprism.spring", //
    "net.geoprism.graph", //
    "net.geoprism.registry.config", //
    "net.geoprism.registry.controller", //
    "net.geoprism.registry.service", //
    "net.geoprism.registry.permission", //
    "net.geoprism.registry.spring", //
    "net.geoprism.email", //
    "net.geoprism.rbac", //
    "net.geoprism.classifier", //
    "net.geoprism.account", //
    "net.geoprism.registry.axon" //
})
@EnableAutoConfiguration
public class SpringAppConfig implements AsyncConfigurer, WebMvcConfigurer

{

  @Bean(name = "multipartResolver")
  public MultipartResolver multipartResolver()
  {
    return new StandardServletMultipartResolver();
  }

  @Bean
  public MultipartConfigElement multipartConfigElement()
  {
    // Unlimited file size (-1 for maxFileSize and maxRequestSize)
    return new MultipartConfigElement("", -1, -1, 0);
  }

  @Override
  public void addCorsMappings(CorsRegistry registry)
  {
    List<String> whitelist = GeoregistryProperties.getCorsWhitelist();

    if (whitelist.contains("*"))
    {
      registry.addMapping("/**").allowedOrigins("*");
    }
    else
    {
      whitelist.forEach(entry -> registry.addMapping("/**").allowedOrigins(entry));
    }
  }

  @Override
  @Bean(name = "taskExecutor")
  public Executor getAsyncExecutor()
  {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(10);
    executor.setQueueCapacity(1000);
    executor.setThreadNamePrefix("async");
    return executor;
  }

  // ---------------> Use this task executor also for async rest methods
  @Bean
  protected WebMvcConfigurer webMvcConfigurer()
  {
    return new WebMvcConfigurer()
    {
      @Override
      public void configureAsyncSupport(AsyncSupportConfigurer configurer)
      {
        configurer.setTaskExecutor(getTaskExecutor());
      }
    };
  }

  @Bean
  protected ConcurrentTaskExecutor getTaskExecutor()
  {
    return new ConcurrentTaskExecutor(this.getAsyncExecutor());
  }

  @Override
  public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler()
  {
    return new SimpleAsyncUncaughtExceptionHandler();
  }

  // @Override
  // public void configureMessageConverters(List<HttpMessageConverter<?>>
  // converters)
  // {
  // GsonHttpMessageConverter msgConverter = new GsonHttpMessageConverter();
  // Gson gson = new GsonBuilder().setPrettyPrinting().create();
  // msgConverter.setGson(gson);
  // converters.add(msgConverter);
  // }

  // @Bean
  // public BeanNameViewResolver beanNameViewResolver()
  // {
  // return new BeanNameViewResolver();
  // }
  //

  // @Bean
  // @Primary
  // Filter sessionFilter()
  // {
  // return new SessionFilter();
  // }

  @Bean
  JsonExceptionHandler exceptionHandler()
  {
    return new JsonExceptionHandler();
  }

  @Bean
  EncodingFilter encodingFilter()
  {
    return new EncodingFilter();
  }
}
