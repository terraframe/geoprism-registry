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
import net.geoprism.externalprofile.business.ExternalProfileBusinessServiceIF;
import net.geoprism.externalprofile.controller.ExternalProfileController;
import net.geoprism.externalprofile.service.ExternalProfileService;
import net.geoprism.externalprofile.service.ExternalProfileServiceIF;
import net.geoprism.forgotpassword.business.ForgotPasswordBusinessServiceIF;
import net.geoprism.forgotpassword.controller.ForgotPasswordController;
import net.geoprism.forgotpassword.service.ForgotPasswordService;
import net.geoprism.forgotpassword.service.ForgotPasswordServiceIF;
import net.geoprism.registry.account.ForgotPasswordCGRService;
import net.geoprism.registry.session.ExternalProfileCGRService;
import net.geoprism.registry.test.MockHttpServletRequest;
import net.geoprism.registry.test.MockHttpServletResponse;
import net.geoprism.session.SessionController;
import net.geoprism.userinvite.controller.UserInviteController;
import net.geoprism.userinvite.service.UserInviteService;

@Configuration
@ComponentScan(basePackages = { "net.geoprism.registry.controller", "net.geoprism.registry.service", "net.geoprism.registry.spring", "net.geoprism.registry.test", "net.geoprism.email", "net.geoprism.rbac", "net.geoprism.classifier" })
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
  
  @Bean
  ForgotPasswordController forgotPasswordController() {
    return new ForgotPasswordController();
  }
  
  @Bean
  ForgotPasswordServiceIF forgotPasswordServiceIF() {
    return new ForgotPasswordService();
  }
  
  @Bean
  ForgotPasswordBusinessServiceIF forgotPasswordBusinessServiceIF() {
    return new ForgotPasswordCGRService();
  }
  
  @Bean
  SessionController sessionController() {
    return new SessionController();
  }
  
  @Bean
  ExternalProfileController externalProfileController() {
    return new ExternalProfileController();
  }
  
  @Bean
  ExternalProfileServiceIF externalProfileServiceIF() {
    return new ExternalProfileService();
  }
  
  @Bean
  ExternalProfileBusinessServiceIF externalProfileBusinessServiceIF() {
    return new ExternalProfileCGRService();
  }
  
  @Bean
  UserInviteController userInviteController() {
    return new UserInviteController();
  }
  
  @Bean
  UserInviteService userInviteService() {
    return new UserInviteService();
  }
  
  @Bean
  PatchingContextListener patchContextListener() {
    return new PatchingContextListener();
  }
  
  @Bean
  RMIContextListener rmiContextListener() {
    return new RMIContextListener();
  }
  
  @Bean
  SchedulerContextListener schedulerContextListener() {
    return new SchedulerContextListener();
  }
}
