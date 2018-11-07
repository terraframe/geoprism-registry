/**
 * Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Runway SDK(tm).
 *
 * Runway SDK(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Runway SDK(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;

import com.runwaysdk.constants.ClientConstants;
import com.runwaysdk.constants.CommonProperties;
import com.runwaysdk.controller.ErrorUtility;

import com.runwaysdk.session.InvalidSessionExceptionDTO;
import com.runwaysdk.web.ServletUtility;
import com.runwaysdk.web.WebClientSession;

public class SessionFilter implements Filter
{
  public void init(FilterConfig filterConfig) throws ServletException
  {
  }

  public void destroy()
  {
  }

  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException
  {
    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) res;

    // response time logging
    req.setAttribute("startTime", (Long) ( new Date().getTime() ));

    HttpSession session = request.getSession();

    WebClientSession clientSession = (WebClientSession) session.getAttribute(ClientConstants.CLIENTSESSION);

    // This isLoggedIn check is not 100% sufficient, it doesn't go to the server
    // and check, it only does it locally, so if the session has expired it'l
    // let it through.
    if (isPublic(request))
    {
      if (clientSession == null)
      {
        Locale[] locales = ServletUtility.getLocales(request);

        clientSession = WebClientSession.createAnonymousSession(locales);

        request.getSession().setMaxInactiveInterval(CommonProperties.getSessionTime());
        request.getSession().setAttribute(ClientConstants.CLIENTSESSION, clientSession);
      }

      req.setAttribute(ClientConstants.CLIENTREQUEST, clientSession.getRequest());

      chain.doFilter(req, res);
      return;
    }
    else if (clientSession != null && clientSession.getRequest().isLoggedIn() && !clientSession.getRequest().isPublicUser())
    {
      try
      {
        req.setAttribute(ClientConstants.CLIENTREQUEST, clientSession.getRequest());

        HttpServletRequest httpServletRequest = (HttpServletRequest) req;
        String path = httpServletRequest.getRequestURI().substring(httpServletRequest.getContextPath().length());

        if (path.equals("/"))
        {
          ( (HttpServletResponse) res ).sendRedirect(httpServletRequest.getContextPath() + "/prism/home");
        }
        else
        {
          chain.doFilter(req, res);
        }
      }
      catch (Throwable t)
      {
        while (t.getCause() != null && !t.getCause().equals(t))
        {
          t = t.getCause();
        }

        if (t instanceof InvalidSessionExceptionDTO)
        {
          response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
          response.addHeader("WWW-Authenticate", "FormBased");

          // If we're asynchronous, we want to return a serialized exception
          if (StringUtils.endsWith(request.getRequestURL().toString(), ".mojax"))
          {
            ErrorUtility.prepareAjaxThrowable(t, response);
          }
          else
          {
            RequestDispatcher dispatcher = request.getRequestDispatcher("/loginRedirect.jsp");
            dispatcher.forward(request, response);

            // // Not an asynchronous request, redirect to the login page.
            // response.sendRedirect(request.getContextPath() + "/loginRedirect");
          }
        }
        else
        {
          if (t instanceof RuntimeException)
          {
            throw (RuntimeException) t;
          }
          else
          {
            throw new RuntimeException(t);
          }
        }
      }

      return;
    }
    else if (pathAllowed(request))
    {
      chain.doFilter(req, res);
      return;
    }
    else
    {
      Cookie cookie = new Cookie("user", "");
      cookie.setMaxAge(0);
      cookie.setPath(request.getContextPath());

      response.addCookie(cookie);
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.addHeader("WWW-Authenticate", "FormBased");

      //

      // The user is not logged in
      // If we're asynchronous, we want to return a serialized exception
      if (StringUtils.endsWith(request.getRequestURL().toString(), ".mojax"))
      {
        ErrorUtility.prepareAjaxThrowable(new InvalidSessionExceptionDTO(), response);
      }
      else
      {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/loginRedirect.jsp");
        dispatcher.forward(request, response);

        // Not an asynchronous request, redirect to the login page.
        // httpRes.sendRedirect(httpReq.getContextPath() + "/loginRedirect");
      }
    }
  }

  private boolean isPublic(HttpServletRequest req)
  {
    String uri = req.getRequestURI();

    Set<String> endpoints = ClientConfigurationService.getPublicEndpoints();

    for (String endpoint : endpoints)
    {
      if (uri.contains(req.getContextPath() + "/" + endpoint))
      {
        return true;
      }
    }

    if (uri.endsWith("Localized.js.jsp"))
    {
      return true;
    }

    return false;
  }

  private boolean pathAllowed(HttpServletRequest req)
  {
    String uri = req.getRequestURI();

    List<String> endpoints = new LinkedList<String>();

    // They're allowed to hit the login view page, otherwise its a redirect loop
    endpoints.add("loginRedirect");

    // They can also invoke the login action on SessionController @
    // session/form and session/login
    endpoints.add("session/form");
    endpoints.add("session/login");
    endpoints.add("session/ologin");

    for (String endpoint : endpoints)
    {
      if (uri.equals(req.getContextPath() + "/" + endpoint))
      {
        return true;
      }
    }

    List<String> directories = new LinkedList<String>();
    directories.add("jquery");
    directories.add("font-awesome");
    directories.add("fontawesome");
    directories.add("3rd-party");

    // Allow direct hitting of all page resources in login directories.
    directories.add("/net/geoprism/login");

    // Directory of uploaded images
    directories.add("uploaded_images/");

    for (String directory : directories)
    {
      if (uri.contains(directory))
      {
        return true;
      }
    }

    List<String> extensions = new LinkedList<String>();
    extensions.add(".sld");
    extensions.add(".css");
    extensions.add(".png");
    extensions.add(".jpg");
    extensions.add(".bmp");
    extensions.add(".jpeg");
    extensions.add(".gif");
    extensions.add(".svg");
    extensions.add(".pdf");
    extensions.add(".otf");
    extensions.add(".mp4");

    extensions.add(".js");

    for (String extension : extensions)
    {
      if (uri.endsWith(extension))
      {
        return true;
      }
    }

    return false;
  }
}
