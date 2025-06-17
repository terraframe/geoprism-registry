package net.geoprism.registry.service.request;

import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;

import jakarta.servlet.MultipartConfigElement;

@Configuration
public class DispatcherServletConfiguration
{
  private String TMP_FOLDER      = "/tmp";

  @Bean
  public DispatcherServlet dispatcherServlet()
  {
    return new DispatcherServlet();
  }

  @Bean
  public DispatcherServletRegistrationBean dispatcherServletRegistration()
  {
    DispatcherServletRegistrationBean registration = new DispatcherServletRegistrationBean(dispatcherServlet(), "/api/");
//    registration.addUrlMappings("/cgr/manage");
    registration.setMultipartConfig(new MultipartConfigElement(TMP_FOLDER, 10485760, 20971520, 5242880));
    registration.setName(DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME);

    return registration;
  }
}