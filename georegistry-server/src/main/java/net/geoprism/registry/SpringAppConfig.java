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
import net.geoprism.externalprofile.business.ExternalProfileBusinessServiceIF;
import net.geoprism.externalprofile.controller.ExternalProfileController;
import net.geoprism.externalprofile.service.ExternalProfileService;
import net.geoprism.externalprofile.service.ExternalProfileServiceIF;
import net.geoprism.forgotpassword.business.ForgotPasswordBusinessServiceIF;
import net.geoprism.forgotpassword.controller.ForgotPasswordController;
import net.geoprism.forgotpassword.service.ForgotPasswordService;
import net.geoprism.forgotpassword.service.ForgotPasswordServiceIF;
import net.geoprism.graph.lpg.business.GeoObjectTypeSnapshotBusinessService;
import net.geoprism.graph.lpg.business.GeoObjectTypeSnapshotBusinessServiceIF;
import net.geoprism.graph.lpg.business.HierarchyTypeSnapshotBusinessService;
import net.geoprism.graph.lpg.business.HierarchyTypeSnapshotBusinessServiceIF;
import net.geoprism.graph.lpg.business.LabeledPropertyGraphTypeBusinessService;
import net.geoprism.graph.lpg.business.LabeledPropertyGraphTypeBusinessServiceIF;
import net.geoprism.graph.lpg.business.LabeledPropertyGraphTypeEntryBusinessService;
import net.geoprism.graph.lpg.business.LabeledPropertyGraphTypeEntryBusinessServiceIF;
import net.geoprism.graph.lpg.business.LabeledPropertyGraphTypeVersionBusinessService;
import net.geoprism.graph.lpg.business.LabeledPropertyGraphTypeVersionBusinessServiceIF;
import net.geoprism.registry.account.ForgotPasswordCGRService;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.lpg.business.GPRGeoObjectTypeSnapshotBusinessService;
import net.geoprism.registry.lpg.business.GPRHierarchyTypeSnapshotBusinessService;
import net.geoprism.registry.lpg.business.GPRLabeledPropertyGraphTypeBusinessService;
import net.geoprism.registry.lpg.business.GPRLabeledPropertyGraphTypeEntryBusinessService;
import net.geoprism.registry.lpg.business.GPRLabeledPropertyGraphTypeVersionBusinessService;
import net.geoprism.registry.session.ExternalProfileCGRService;
import net.geoprism.session.LoginBruteForceGuardService;
import net.geoprism.session.SessionController;
import net.geoprism.session.SessionFilter;
import net.geoprism.spring.JsonExceptionHandler;
import net.geoprism.userinvite.controller.UserInviteController;
import net.geoprism.userinvite.service.UserInviteService;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = { "net.geoprism.spring", "net.geoprism.graph", "net.geoprism.registry.controller", "net.geoprism.registry.service", "net.geoprism.registry.spring", "net.geoprism.email", "net.geoprism.rbac", "net.geoprism.classifier", "net.geoprism.account" })
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

  @Bean
  ForgotPasswordController forgotPasswordController()
  {
    return new ForgotPasswordController();
  }

  @Bean
  ForgotPasswordServiceIF forgotPasswordServiceIF()
  {
    return new ForgotPasswordService();
  }

  @Bean
  ForgotPasswordBusinessServiceIF forgotPasswordBusinessServiceIF()
  {
    return new ForgotPasswordCGRService();
  }

  @Bean
  SessionController sessionController()
  {
    return new SessionController();
  }

  @Bean
  ExternalProfileController externalProfileController()
  {
    return new ExternalProfileController();
  }

  @Bean
  ExternalProfileServiceIF externalProfileServiceIF()
  {
    return new ExternalProfileService();
  }

  @Bean
  ExternalProfileBusinessServiceIF externalProfileBusinessServiceIF()
  {
    return new ExternalProfileCGRService();
  }

  @Bean
  UserInviteController userInviteController()
  {
    return new UserInviteController();
  }

  @Bean
  UserInviteService userInviteService()
  {
    return new UserInviteService();
  }

  @Bean
  LoginBruteForceGuardService loginBruteForceGuard()
  {
    return new LoginBruteForceGuardService();
  }

  @Bean
  GeoObjectTypeSnapshotBusinessService geoObjectTypeSnapshotBusinessService()
  {
    return new GPRGeoObjectTypeSnapshotBusinessService();
  }

  @Bean
  HierarchyTypeSnapshotBusinessService hierarchyTypeSnapshotBusinessService()
  {
    return new GPRHierarchyTypeSnapshotBusinessService();
  }

  @Bean
  LabeledPropertyGraphTypeBusinessService labeledPropertyGraphTypeBusinessService()
  {
    return new GPRLabeledPropertyGraphTypeBusinessService();
  }

  @Bean
  LabeledPropertyGraphTypeEntryBusinessService labeledPropertyGraphTypeEntryBusinessService()
  {
    return new GPRLabeledPropertyGraphTypeEntryBusinessService();
  }

  @Bean
  LabeledPropertyGraphTypeVersionBusinessService labeledPropertyGraphTypeVersionBusinessService()
  {
    return new GPRLabeledPropertyGraphTypeVersionBusinessService();
  }

}
