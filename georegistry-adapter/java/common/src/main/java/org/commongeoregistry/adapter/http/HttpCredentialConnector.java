/**
 *
 */
package org.commongeoregistry.adapter.http;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpCredentialConnector extends AbstractHttpConnector
{
  protected String username;

  protected String password;

  public void setCredentials(String username, String password)
  {
    this.username = username;
    this.password = password;
  }
  
  synchronized public void initialize()
  {
    // TODO : This trust manager trusts everyone. This makes us vulnerable to MITM attacks.
    class DefaultTrustManager implements X509TrustManager
    {

      @Override
      public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException
      {
      }

      @Override
      public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException
      {
      }

      @Override
      public X509Certificate[] getAcceptedIssuers()
      {
        return null;
      }
    }
    
    class AdapterHostNameVerifier implements HostnameVerifier {
  
        @Override   
        public boolean verify(String hostname, SSLSession session) {
          if (HttpCredentialConnector.this.getServerUrl().contains(hostname))
          {
            return true;
          }
          else
          {
            // TODO : Do we log this?
            System.out.println("Rejecting hostname [" + hostname + "].");
            return false;
          }
        }
  
    }
    
    try
    {
      SSLContext ctx = SSLContext.getInstance("TLS");

      ctx.init(new KeyManager[0], new TrustManager[] { new DefaultTrustManager() }, new SecureRandom());

      SSLContext.setDefault(ctx);
      
      HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());
      HttpsURLConnection.setDefaultHostnameVerifier(new AdapterHostNameVerifier());
    }
    catch (KeyManagementException | NoSuchAlgorithmException e)
    {
      throw new RuntimeException(e);
    }

    /*
     * Set the default authenticator for the VM
     */
    Authenticator.setDefault(new Authenticator()
    {
      protected PasswordAuthentication getPasswordAuthentication()
      {
        return new PasswordAuthentication(username, password.toCharArray());
      }
    });
  }
}
