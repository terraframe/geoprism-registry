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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.MultipartConfigElement;
import net.geoprism.EncodingFilter;
import net.geoprism.spring.web.JsonExceptionHandler;

@Configuration
@EnableWebMvc
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
public class SpringAppConfig implements WebMvcConfigurer, AsyncConfigurer

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

  // @Override
  // public void configureMessageConverters(List<HttpMessageConverter<?>>
  // converters)
  // {
  // GsonHttpMessageConverter msgConverter = new GsonHttpMessageConverter();
  // Gson gson = new GsonBuilder().setPrettyPrinting().create();
  // msgConverter.setGson(gson);
  // converters.add(msgConverter);
  // }

  // @Override
  // public void addResourceHandlers(ResourceHandlerRegistry registry)
  // {
  // registry.addResourceHandler("index.html").addResourceLocations("/index.html
  // ");
  // registry.addResourceHandler("/index.html").addResourceLocations("/index.html
  // ");
  // }

//  @Bean
//  public BeanNameViewResolver beanNameViewResolver()
//  {
//    return new BeanNameViewResolver();
//  }
//
//  @Bean
//  public View index()
//  {
//    return new JstlView("/static/index.html");
//  }

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
