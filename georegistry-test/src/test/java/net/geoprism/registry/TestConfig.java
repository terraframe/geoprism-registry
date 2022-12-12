package net.geoprism.registry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import net.geoprism.registry.test.MockHttpServletRequest;
import net.geoprism.registry.test.MockHttpServletResponse;

@Configuration
@ComponentScan(basePackages = { "net.geoprism.registry.controller", "net.geoprism.registry.service", "net.geoprism.registry.spring", "net.geoprism.registry.test" })
public class TestConfig
{

  
  @Bean
  HttpServletRequest request(){
    return new MockHttpServletRequest();
  }
  
  @Bean
  HttpServletResponse response(){
    return new MockHttpServletResponse();
  }
}
