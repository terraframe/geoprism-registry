/**
 *
 */
package org.commongeoregistry.adapter;

public class RequiredParameterException extends RuntimeException
{
  /**
   * 
   */
  private static final long serialVersionUID = 4747172271720334348L;

  /**
   * 
   */

  private String            parameter;

  public RequiredParameterException(String methodName, String parameter)
  {
    super("Method [" + methodName + "] requires a parameter value for the parameter named [" + parameter + "]");

    this.parameter = parameter;
  }

  public String getParameter()
  {
    return parameter;
  }
}
