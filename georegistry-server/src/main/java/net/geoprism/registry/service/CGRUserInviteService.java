package net.geoprism.registry.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.commongeoregistry.adapter.metadata.RegistryRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.localization.LocalizationFacade;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.Roles;

import net.geoprism.account.UserInviteService;
import net.geoprism.account.UserInviteServiceIF;
import net.geoprism.configuration.GeoprismProperties;
import net.geoprism.registry.Organization;
import net.geoprism.registry.UserInfo;
import net.geoprism.registry.conversion.RegistryRoleConverter;

@Component
public class CGRUserInviteService extends UserInviteService implements UserInviteServiceIF
{

  private static final Logger logger = LoggerFactory.getLogger(CGRUserInviteService.class);
  
  @Override
  protected void applyUserWithRoles(JsonObject joUser, List<String> roleNames)
  {
    UserInfo.applyUserWithRoles(joUser, roleNames.toArray(new String[roleNames.size()]), true);
  }

  @Override
  protected void sendEmail(String emailAddress, String token, JsonArray roleNameArray)
  {
    final String serverExternalUrl = GeoprismProperties.getRemoteServerUrl();
    
    String link = serverExternalUrl + "#/admin/invite-complete/" + token;

    String subject = LocalizationFacade.localize("user.invite.email.subject");
    
    String body = LocalizationFacade.localize("user.invite.email.body");
    body = body.replaceAll("\\\\n", "\n");
    body = body.replace("${link}", link);
    body = body.replace("${expireTime}", getLocalizedExpireTime());
    
    String orgLabel = "??";
    Set<String> roleLabels = new HashSet<String>();
    
    for (int i = 0; i < roleNameArray.size(); ++i)
    {
      String roleName = roleNameArray.get(i).getAsString();
      
      Roles role = Roles.findRoleByName(roleName);
      
      RegistryRole registryRole = new RegistryRoleConverter().build(role);
      
      if (orgLabel.equals("??"))
      {
        String orgCode = registryRole.getOrganizationCode();
        if (orgCode != null && orgCode.length() > 0)
        {
          orgLabel = Organization.getByCode(orgCode).getDisplayLabel().getValue().trim();
        }
      }
      
      String roleLabel;
      if (RegistryRole.Type.isRA_Role(roleName))
      {
        roleLabel = Roles.findRoleByName("cgr.RegistryAdministrator").getDisplayLabel().getValue().trim();
      }
      else
      {
        roleLabel = role.getDisplayLabel().getValue().trim();
      }
      
      roleLabels.add(roleLabel);
    }
    
    body = body.replace("${roles}", StringUtils.join(roleLabels, ", "));
    
    if (orgLabel.equals("??")) {
      orgLabel = StringUtils.join(roleLabels, ", ");
    }
    body = body.replace("${organization}", orgLabel);

    this.emailService.sendEmail(Session.getCurrentSession().getOid(), subject, body, new String[] { emailAddress });
  }
  
}
