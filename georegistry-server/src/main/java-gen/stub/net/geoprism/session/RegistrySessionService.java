package net.geoprism.session;

import java.util.Base64;

import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest.TokenRequestBuilder;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.rbac.Authenticate;
import com.runwaysdk.business.rbac.SingleActorDAOIF;
import com.runwaysdk.business.rbac.UserDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.InvalidLoginException;
import com.runwaysdk.session.SessionFacade;
import com.runwaysdk.system.Users;
import com.runwaysdk.system.UsersQuery;

import net.geoprism.GeoprismUser;
import net.geoprism.account.LocaleSerializer;
import net.geoprism.account.OauthServer;
import net.geoprism.registry.UserInfo;
import net.geoprism.registry.graph.DHIS2ExternalSystem;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.session.UserNotFoundException;
import net.geoprism.registry.session.UserNotOuathEnabledException;

public class RegistrySessionService extends RegistrySessionServiceBase
{
  private static final long serialVersionUID = -75045565;
  
  private static final Logger logger = LoggerFactory.getLogger(RegistrySessionService.class);

  public RegistrySessionService()
  {
    super();
  }

  /**
   * Serves as a "redirect url" for logging into DHIS2 via oauth.
   * 
   * @param serverId
   * @param code
   * @param locales
   * @param redirectBase
   * @return
   */
  @Authenticate
  public static java.lang.String ologin(java.lang.String serverId, java.lang.String code, java.lang.String locales, java.lang.String redirectBase)
  {
    try
    {
      String redirect = redirectBase + "/cgrsession/ologin";

      OauthServer server = OauthServer.get(serverId);
      /*
       * Get the access token
       */
      TokenRequestBuilder tokenBuilder = OAuthClientRequest.tokenLocation(server.getTokenLocation());
      tokenBuilder.setGrantType(GrantType.AUTHORIZATION_CODE);
      tokenBuilder.setRedirectURI(redirect);
      tokenBuilder.setCode(code);

      String auth = server.getClientId() + ":" + server.getSecretKey();

      OAuthClientRequest tokenRequest = tokenBuilder.buildBodyMessage();
      tokenRequest.setHeader("Accept", "application/json");
      tokenRequest.setHeader("Authorization", "Basic " + new String(Base64.getEncoder().encode(auth.getBytes())));

      URLConnectionClient connClient = new URLConnectionClient();
      OAuthClient oAuthClient = new OAuthClient(connClient);
      OAuthJSONAccessTokenResponse accessToken = oAuthClient.accessToken(tokenRequest, OAuth.HttpMethod.POST, OAuthJSONAccessTokenResponse.class);

      /*
       * Request the user information
       */
      OAuthBearerClientRequest requestBuilder = new OAuthBearerClientRequest(server.getProfileLocation());
      requestBuilder.setAccessToken(accessToken.getAccessToken());

      OAuthClientRequest bearerRequest = requestBuilder.buildQueryMessage();
      OAuthResourceResponse resourceResponse = oAuthClient.resource(bearerRequest, OAuth.HttpMethod.GET, OAuthResourceResponse.class);

      String body = resourceResponse.getBody();

      JSONObject object = new JSONObject(body);
      
      final String username = object.getJSONObject("userCredentials").getString("username");

      SingleActorDAOIF profile = RegistrySessionService.getActor(server, username);

      String sessionId = SessionFacade.logIn(profile, LocaleSerializer.deserialize(locales));
      
      JsonObject json = new JsonObject();
      json.addProperty("sessionId", sessionId);
      json.addProperty("username", username);
      return json.toString();
    }
    catch (JSONException | OAuthSystemException | OAuthProblemException e)
    {
      throw new InvalidLoginException(e);
    }
  }

  @Transaction
  private static synchronized SingleActorDAOIF getActor(OauthServer server, String username) throws JSONException
  {
    UsersQuery query = new UsersQuery(new QueryFactory());
    query.WHERE(query.getUsername().EQ(username));
    OIterator<? extends Users> it = query.getIterator();

    try
    {
      if (it.hasNext())
      {
        UserDAO user = (UserDAO) BusinessFacade.getEntityDAO(it.next());
        
        try
        {
          GeoprismUser geoprismUser = GeoprismUser.getByUsername(user.getUsername());
          
          UserInfo userInfo = UserInfo.getByUser(geoprismUser);
          
          ExternalSystem system = ExternalSystem.get(userInfo.getExternalSystemOid());
          
          if (system instanceof DHIS2ExternalSystem)
          {
            DHIS2ExternalSystem dhis2System = (DHIS2ExternalSystem) system;
            
            if (dhis2System.getOauthServerOid().equals(server.getOid()))
            {
              return user;
            }
          }
        }
        catch (Throwable t)
        {
          logger.error("Encountered an unexpected error while logging user in.", t);
        }
        
        UserNotOuathEnabledException ex = new UserNotOuathEnabledException();
        ex.setUsername(user.getUsername());
        ex.setOauthServer(server.getDisplayLabel().getValue());
        throw ex;
      }
      else
      {
        UserNotFoundException ex = new UserNotFoundException();
        ex.setUsername(username);
        throw ex;
      }
    }
    finally
    {

    }
  }

  // ExternalProfileQuery query = new ExternalProfileQuery(new QueryFactory());
  // query.WHERE(query.getUsername().EQ(username));
  // OIterator<? extends ExternalProfile> it = query.getIterator();
  //
  // try
  // {
  // if (it.hasNext())
  // {
  // ExternalProfile profile = it.next();
  // profile.lock();
  // OauthServer.populate(serverType, profile, object);
  // profile.apply();
  //
  // return (SingleActorDAOIF) BusinessFacade.getEntityDAO(profile);
  // }
  // else
  // {
  // UserNotFoundException ex = new UserNotFoundException();
  // ex.setUsername(username);
  // throw ex;
  // }
  // }
  // finally
  // {
  // it.close();
  // }
}
