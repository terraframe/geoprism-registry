package net.geoprism.registry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import net.geoprism.ForgotPasswordController;
import net.geoprism.ForgotPasswordServiceIF;
import net.geoprism.account.UserInviteController;
import net.geoprism.classifier.ClassifierService;
import net.geoprism.classifier.ClassifierServiceIF;
import net.geoprism.email.EmailController;
import net.geoprism.email.EmailService;
import net.geoprism.email.EmailServiceIF;
import net.geoprism.rbac.RoleService;
import net.geoprism.rbac.RoleServiceIF;
import net.geoprism.registry.account.ForgotPasswordCGRService;
import net.geoprism.registry.session.ExternalProfileCGRService;
import net.geoprism.registry.test.MockHttpServletRequest;
import net.geoprism.registry.test.MockHttpServletResponse;
import net.geoprism.session.ExternalProfileController;
import net.geoprism.session.ExternalProfileServiceIF;

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
  
  @Bean
  ForgotPasswordServiceIF forgotPasswordServiceIF() {
    return new ForgotPasswordCGRService();
  }
  
  @Bean
  ForgotPasswordController forgotPasswordController() {
    return new ForgotPasswordController();
  }
  
  @Bean
  ExternalProfileServiceIF externalProfileServiceIF() {
    return new ExternalProfileCGRService();
  }
  
  @Bean
  ClassifierServiceIF classifierServiceIF() {
    return new ClassifierService();
  }
  
  @Bean
  RoleServiceIF roleServiceIF() {
    return new RoleService();
  }
  
  @Bean
  ExternalProfileController externalProfileController() {
    return new ExternalProfileController();
  }
  
  @Bean
  EmailServiceIF emailService() {
    return new EmailService();
  }
  
  @Bean
  EmailController emailController() {
    return new EmailController();
  }
  
  @Bean
  UserInviteController userInviteController() {
    return new UserInviteController();
  }
}
