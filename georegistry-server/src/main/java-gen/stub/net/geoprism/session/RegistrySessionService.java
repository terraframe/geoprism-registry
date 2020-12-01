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

import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.rbac.Authenticate;
import com.runwaysdk.business.rbac.SingleActorDAOIF;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.InvalidLoginException;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.SessionFacade;

import net.geoprism.account.ExternalProfile;
import net.geoprism.account.ExternalProfileQuery;
import net.geoprism.account.LocaleSerializer;
import net.geoprism.account.OauthServer;
import net.geoprism.registry.session.UserNotFoundException;

public class RegistrySessionService extends RegistrySessionServiceBase
{
  private static final long serialVersionUID = -75045565;
  
  public RegistrySessionService()
  {
    super();
  }
  
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
      
//    tokenBuilder.setClientId(server.getClientId());
//    tokenBuilder.setClientSecret(server.getSecretKey());
//      tokenBuilder.setUsername(server.getClientId());
//      tokenBuilder.setPassword(server.getSecretKey());
      
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

      SingleActorDAOIF profile = RegistrySessionService.getOrCreateActor(server, object);

      return SessionFacade.logIn(profile, LocaleSerializer.deserialize(locales));
    }
    catch (JSONException | OAuthSystemException | OAuthProblemException e)
    {
      throw new InvalidLoginException(e);
    }
  }
  
  @Transaction
  private static synchronized SingleActorDAOIF getOrCreateActor(OauthServer server, JSONObject object) throws JSONException
  {
    String serverType = server.getServerType();

    String remoteId = OauthServer.getRemoteId(serverType, object);

    ExternalProfileQuery query = new ExternalProfileQuery(new QueryFactory());
    query.WHERE(query.getRemoteId().EQ(remoteId));
    OIterator<? extends ExternalProfile> it = query.getIterator();

    try
    {
      if (it.hasNext())
      {
        ExternalProfile profile = it.next();
        profile.lock();
        OauthServer.populate(serverType, profile, object);
        profile.apply();

        return (SingleActorDAOIF) BusinessFacade.getEntityDAO(profile);
      }
      else
      {
        UserNotFoundException ex = new UserNotFoundException();
//        ex.setUsername();
        throw ex;
      }
    }
    finally
    {
      it.close();
    }
  }
}
