package net.geoprism.registry.service.request;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties;
import org.springframework.web.context.WebApplicationContext;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;

import jakarta.servlet.MultipartConfigElement;

@Configuration
public class DispatcherServletConfiguration
{
  private String TMP_FOLDER      = "/tmp";

//  @Bean
//  public DispatcherServlet dispatcherServlet(WebApplicationContext parentContext)
//  {
//    return new DispatcherServlet(parentContext);
//  }
//
//  @Bean
//  public DispatcherServletRegistrationBean dispatcherServletRegistration(WebApplicationContext parentContext)
//  {
//    DispatcherServletRegistrationBean registration = new DispatcherServletRegistrationBean(dispatcherServlet(parentContext), "/cgr/manage/*");
//    registration.setMultipartConfig(new MultipartConfigElement(TMP_FOLDER, 10485760, 20971520, 5242880));
//    registration.setName(DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME);
//
//    return registration;
//  }
//  
//  @Bean
//  public ServletRegistrationBean<DispatcherServlet> cgrDispatcherServletRegistration(WebApplicationContext parentContext) {
//      DispatcherServlet cgrServlet = new DispatcherServlet(parentContext);
//
//      ServletRegistrationBean<DispatcherServlet> registration =
//          new ServletRegistrationBean<>(cgrServlet, "/api/");
//
//      registration.setName("cgrDispatcherServlet");
//      registration.setLoadOnStartup(1);
//      registration.setMultipartConfig(new MultipartConfigElement(TMP_FOLDER, 10485760, 20971520, 5242880));
//      return registration;
//  }
}