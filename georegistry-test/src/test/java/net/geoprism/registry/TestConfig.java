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
import net.geoprism.externalprofile.business.ExternalProfileBusinessService;
import net.geoprism.externalprofile.controller.ExternalProfileController;
import net.geoprism.externalprofile.service.ExternalProfileService;
import net.geoprism.externalprofile.service.ExternalProfileService;
import net.geoprism.forgotpassword.business.ForgotPasswordBusinessService;
import net.geoprism.forgotpassword.controller.ForgotPasswordController;
import net.geoprism.forgotpassword.service.ForgotPasswordService;
import net.geoprism.forgotpassword.service.ForgotPasswordService;
import net.geoprism.graph.lpg.business.GeoObjectTypeSnapshotBusinessService;
import net.geoprism.graph.lpg.business.HierarchyTypeSnapshotBusinessService;
import net.geoprism.graph.lpg.business.LabeledPropertyGraphTypeBusinessService;
import net.geoprism.graph.lpg.business.LabeledPropertyGraphTypeEntryBusinessService;
import net.geoprism.graph.lpg.business.LabeledPropertyGraphTypeVersionBusinessService;
import net.geoprism.registry.account.ForgotPasswordCGRService;
import net.geoprism.registry.lpg.business.GPRGeoObjectTypeSnapshotBusinessService;
import net.geoprism.registry.lpg.business.GPRHierarchyTypeSnapshotBusinessService;
import net.geoprism.registry.lpg.business.GPRLabeledPropertyGraphTypeBusinessService;
import net.geoprism.registry.lpg.business.GPRLabeledPropertyGraphTypeEntryBusinessService;
import net.geoprism.registry.lpg.business.GPRLabeledPropertyGraphTypeVersionBusinessService;
import net.geoprism.registry.session.ExternalProfileCGRService;
import net.geoprism.registry.test.MockHttpServletRequest;
import net.geoprism.registry.test.MockHttpServletResponse;
import net.geoprism.session.LoginBruteForceGuardService;
import net.geoprism.session.SessionController;
import net.geoprism.userinvite.controller.UserInviteController;
import net.geoprism.userinvite.service.UserInviteService;

@Configuration
@ComponentScan(basePackages = { "net.geoprism.spring", "net.geoprism.graph", "net.geoprism.registry.controller", "net.geoprism.registry.service", "net.geoprism.registry.spring", "net.geoprism.registry.test", "net.geoprism.email", "net.geoprism.rbac", "net.geoprism.classifier", "net.geoprism.account" })
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
  ForgotPasswordController forgotPasswordController()
  {
    return new ForgotPasswordController();
  }

  @Bean
  ForgotPasswordService forgotPasswordService()
  {
    return new ForgotPasswordService();
  }

  @Bean
  ForgotPasswordBusinessService forgotPasswordBusinessService()
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
  ExternalProfileService externalProfileService()
  {
    return new ExternalProfileService();
  }

  @Bean
  ExternalProfileBusinessService externalProfileBusinessService()
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
