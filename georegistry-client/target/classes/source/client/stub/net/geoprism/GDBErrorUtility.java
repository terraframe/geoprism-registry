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

import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.runwaysdk.AttributeNotificationDTO;
import com.runwaysdk.ProblemExceptionDTO;
import com.runwaysdk.business.ProblemDTOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorExceptionDTO;


public class GDBErrorUtility 
{
  public static final String ERROR_MESSAGE_ARRAY = "errorMessageArray";

  public static final String ERROR_MESSAGE       = "errorMessage";

  public static final String DEVELOPER_MESSAGE   = "developerMessage";

  public static final String MESSAGE_ARRAY       = "messageArray";

  public static void prepareProblems(ProblemExceptionDTO e, HttpServletRequest req, boolean ignoreNotifications)
  {
    List<String> messages = new LinkedList<String>();

    for (ProblemDTOIF problem : e.getProblems())
    {
      if ((!ignoreNotifications) && ( problem instanceof AttributeNotificationDTO ))
      {
        String message = problem.getMessage();

        messages.add(message);
      }
    }

    if (messages.size() > 0)
    {
      req.setAttribute(ERROR_MESSAGE_ARRAY, messages.toArray(new String[messages.size()]));
    }
  }
  
  public static void prepareThrowable(Throwable t, HttpServletRequest req)
  {
    String localizedMessage = t.getLocalizedMessage();

    req.setAttribute(ERROR_MESSAGE, localizedMessage);

    if (t instanceof ProgrammingErrorExceptionDTO)
    {
        ProgrammingErrorExceptionDTO pee = (ProgrammingErrorExceptionDTO) t;
        String developerMessage = pee.getDeveloperMessage();

        req.setAttribute(DEVELOPER_MESSAGE, developerMessage);

    }
  }
  
}
