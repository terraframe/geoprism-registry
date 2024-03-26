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
package net.geoprism.registry;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import javax.servlet.Filter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.format.datetime.DateFormatterRegistrar;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.view.BeanNameViewResolver;
import org.springframework.web.servlet.view.JstlView;

import net.geoprism.EncodingFilter;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.service.SessionFilter;
import net.geoprism.spring.web.JsonExceptionHandler;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = { "net.geoprism.spring", "net.geoprism.graph", "net.geoprism.registry.controller", "net.geoprism.registry.service", "net.geoprism.registry.permission", "net.geoprism.registry.spring", "net.geoprism.email", "net.geoprism.rbac", "net.geoprism.classifier", "net.geoprism.account" })
public class SpringAppConfig extends WebMvcConfigurationSupport
{

  @Bean(name = "multipartResolver")
  public CommonsMultipartResolver multipartResolver()
  {
    CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
    multipartResolver.setMaxUploadSize(-1);
    return multipartResolver;
  }

  @Bean
  @Override
  public FormattingConversionService mvcConversionService()
  {
    DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService(false);

    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(GeoObjectImportConfiguration.DATE_FORMAT);
    dateTimeFormatter.withZone(ZoneId.of(GeoRegistryUtil.SYSTEM_TIMEZONE.getID()));

    DateTimeFormatterRegistrar dateTimeRegistrar = new DateTimeFormatterRegistrar();
    dateTimeRegistrar.setDateFormatter(dateTimeFormatter);
    dateTimeRegistrar.setDateTimeFormatter(dateTimeFormatter);
    dateTimeRegistrar.registerFormatters(conversionService);

    DateFormatter dateFormatter = new DateFormatter(GeoObjectImportConfiguration.DATE_FORMAT);
    dateFormatter.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

    DateFormatterRegistrar dateRegistrar = new DateFormatterRegistrar();
    dateRegistrar.setFormatter(dateFormatter);
    dateRegistrar.registerFormatters(conversionService);

    return conversionService;
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

  @Bean
  public BeanNameViewResolver beanNameViewResolver()
  {
    return new BeanNameViewResolver();
  }

  @Bean
  public View index()
  {
    return new JstlView("../index.html");
  }

  @Bean
  Filter sessionFilter()
  {
    return new SessionFilter();
  }

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
