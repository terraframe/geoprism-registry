/**
 *
 */
package net.geoprism.registry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import net.geoprism.PatchingContextListener;
import net.geoprism.RMIContextListener;
import net.geoprism.SchedulerContextListener;
import net.geoprism.registry.test.MockHttpServletRequest;
import net.geoprism.registry.test.MockHttpServletResponse;

@Configuration
@ComponentScan(basePackages = { "net.geoprism.registry.service", "net.geoprism.registry.controller", "net.geoprism.spring", "net.geoprism.registry.spring", "net.geoprism.registry.test", "net.geoprism.email", "net.geoprism.rbac", "net.geoprism.classifier", "net.geoprism.account" })
public class TestConfig
{

  @Bean
  HttpServletRequest request()
  {
    return new MockHttpServletRequest();
  }

  @Bean
  HttpServletResponse response()
  {
    return new MockHttpServletResponse();
  }

  @Bean
  PatchingContextListener patchContextListener()
  {
    return new PatchingContextListener();
  }

  @Bean
  RMIContextListener rmiContextListener()
  {
    return new RMIContextListener();
  }

  @Bean
  SchedulerContextListener schedulerContextListener()
  {
    return new SchedulerContextListener();
  }

}
