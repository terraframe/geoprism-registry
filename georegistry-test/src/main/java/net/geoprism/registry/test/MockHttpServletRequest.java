/**
 *
 */
package net.geoprism.registry.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConnection;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpUpgradeHandler;
import jakarta.servlet.http.Part;

import com.runwaysdk.constants.ClientConstants;
import com.runwaysdk.constants.ClientRequestIF;

public class MockHttpServletRequest implements HttpServletRequest
{
  private static ClientRequestIF clientRequest;

  public static synchronized void setClientRequest(ClientRequestIF cr)
  {
    clientRequest = cr;
  }

  public static synchronized ClientRequestIF getClientRequest()
  {
    return clientRequest;
  }

  @Override
  public Object getAttribute(String name)
  {
    if (name.equals(ClientConstants.CLIENTREQUEST))
    {
      return clientRequest;
    }

    return null;
  }

  @Override
  public Enumeration<String> getAttributeNames()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getCharacterEncoding()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setCharacterEncoding(String env) throws UnsupportedEncodingException
  {
    // TODO Auto-generated method stub

  }

  @Override
  public int getContentLength()
  {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public String getContentType()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ServletInputStream getInputStream() throws IOException
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getParameter(String name)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Enumeration<String> getParameterNames()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String[] getParameterValues(String name)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<String, String[]> getParameterMap()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getProtocol()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getScheme()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getServerName()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int getServerPort()
  {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public BufferedReader getReader() throws IOException
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getRemoteAddr()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getRemoteHost()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setAttribute(String name, Object o)
  {
    // TODO Auto-generated method stub

  }

  @Override
  public void removeAttribute(String name)
  {
    // TODO Auto-generated method stub

  }

  @Override
  public Locale getLocale()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Enumeration<Locale> getLocales()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isSecure()
  {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public RequestDispatcher getRequestDispatcher(String path)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int getRemotePort()
  {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public String getLocalName()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getLocalAddr()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int getLocalPort()
  {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public ServletContext getServletContext()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public AsyncContext startAsync() throws IllegalStateException
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isAsyncStarted()
  {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isAsyncSupported()
  {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public AsyncContext getAsyncContext()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public DispatcherType getDispatcherType()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getAuthType()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Cookie[] getCookies()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public long getDateHeader(String name)
  {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public String getHeader(String name)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Enumeration<String> getHeaders(String name)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Enumeration<String> getHeaderNames()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int getIntHeader(String name)
  {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public String getMethod()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getPathInfo()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getPathTranslated()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getContextPath()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getQueryString()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getRemoteUser()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isUserInRole(String role)
  {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Principal getUserPrincipal()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getRequestedSessionId()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getRequestURI()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public StringBuffer getRequestURL()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getServletPath()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public HttpSession getSession(boolean create)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public HttpSession getSession()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isRequestedSessionIdValid()
  {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isRequestedSessionIdFromCookie()
  {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isRequestedSessionIdFromURL()
  {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean authenticate(HttpServletResponse response) throws IOException, ServletException
  {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void login(String username, String password) throws ServletException
  {
    // TODO Auto-generated method stub

  }

  @Override
  public void logout() throws ServletException
  {
    // TODO Auto-generated method stub

  }

  @Override
  public Collection<Part> getParts() throws IOException, ServletException
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Part getPart(String name) throws IOException, ServletException
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public long getContentLengthLong()
  {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public String getRequestId()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getProtocolRequestId()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ServletConnection getServletConnection()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String changeSessionId()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException
  {
    // TODO Auto-generated method stub
    return null;
  }

}
