package net.geoprism.dhis2.dhis2adapter;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.X509Certificate;

/**
 * This class contains utility methods for modifying the SSL certificate validation behavior. Use with caution as improper
 * use may introduce security holes.
 * 
 * Instead of using this class, you may just want to modify the truststore used for dev. (check out src/test/resources/howto.txt
 * for more info).
 * 
 * @author rrowlands
 *
 */
public class SSLTrustConfiguration
{
  public static void trustLocalhost()
  {
      HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();

      HostnameVerifier allHostsValid = new HostnameVerifier()
      {
        public boolean verify(String hostname, SSLSession session)
        {
          if (hostname.equals("127.0.0.1") || hostname.equals("localhost"))
          {
            return true;
          }
          else
          {
            return hv.verify(hostname, session);
          }
        }
      };

      HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
  }
  
  public static void trustAll()
  {
    try
    {
      // Create a trust manager that does not validate certificate chains
      TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager()
      {
        public java.security.cert.X509Certificate[] getAcceptedIssuers()
        {
          return null;
        }
  
        @Override
        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException
        {
          
        }

        @Override
        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException
        {
          
        }
      } };
  
      // Install the all-trusting trust manager
      SSLContext sc = SSLContext.getInstance("SSL");
      sc.init(null, trustAllCerts, new java.security.SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    
      // Create a host name verifier which trusts localhost
      HostnameVerifier allHostsValid = new HostnameVerifier()
      {
        public boolean verify(String hostname, SSLSession session)
        {
          return true;
        }
      };
  
      // Install the localhost-trusting host verifier
      HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }
    catch (NoSuchAlgorithmException e)
    {
      e.printStackTrace();
    }
    catch (KeyManagementException e)
    {
      e.printStackTrace();
    }
  }
}
