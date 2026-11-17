/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.etl;

public class ImportFileFormatException extends ImportFileFormatExceptionBase
{
  private static final long serialVersionUID = 322454825;
  
  public ImportFileFormatException()
  {
    super();
  }
  
  public ImportFileFormatException(java.lang.String developerMessage)
  {
    super(developerMessage);
  }
  
  public ImportFileFormatException(java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(developerMessage, cause);
  }
  
  public ImportFileFormatException(java.lang.Throwable cause)
  {
    super(cause);
  }
  
  /**
   * Longest details message shown to the user
   */
  private static final int MAX_DETAILS_LENGTH = 500;
  
  /**
   * @param details
   *          Why the file could not be read, shown to the user as is (it is
   *          not localized)
   */
  public static ImportFileFormatException withDetails(String details)
  {
    return withDetails(details, null);
  }
  
  public static ImportFileFormatException withDetails(String details, Throwable cause)
  {
    ImportFileFormatException ex = new ImportFileFormatException(details, cause);
    ex.setDetails(details);
    
    return ex;
  }
  
  /**
   * Creates an exception whose details are the message of the underlying
   * error, e.g. the parser's description of what is wrong with the file.
   */
  public static ImportFileFormatException fromCause(Throwable cause)
  {
    return withDetails(describe(cause), cause);
  }
  
  /**
   * @return The most specific message of the error: the first message in the
   *         cause chain which isn't just a wrapper around its cause's message
   *         (e.g. new RuntimeException(cause)), limited to its first line and
   *         {@link #MAX_DETAILS_LENGTH} characters
   */
  public static String describe(Throwable throwable)
  {
    String message = null;
    
    for (Throwable t = throwable; t != null && message == null; t = ( t.getCause() == t ? null : t.getCause() ))
    {
      String m = t.getMessage();
      Throwable cause = t.getCause();
      
      boolean wrapper = cause != null && cause != t && ( m == null || m.equals(cause.toString()) || m.equals(cause.getMessage()) );
      
      if (m != null && !m.isBlank() && !wrapper)
      {
        message = m;
      }
    }
    
    if (message == null)
    {
      return throwable.getClass().getSimpleName();
    }
    
    message = message.strip().lines().findFirst().orElse(message).strip();
    
    if (message.length() > MAX_DETAILS_LENGTH)
    {
      message = message.substring(0, MAX_DETAILS_LENGTH) + "...";
    }
    
    return message;
  }
  
}
