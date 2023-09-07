/**
 *
 */
package org.commongeoregistry.adapter.dataaccess;

import org.commongeoregistry.adapter.metadata.AttributeType;

public class UnknownTermException extends RuntimeException
{
  /**
   * 
   */
  private static final long serialVersionUID = 5427517299215959878L;

  private String            code;

  private AttributeType     attribute;

  public UnknownTermException(String code, AttributeType attribute)
  {
    super("The term code [" + code + "] does not correspond to a known term for attribute [" + attribute.getLabel().getValue() + "]");

    this.code = code;
    this.attribute = attribute;
  }

  public String getCode()
  {
    return code;
  }

  public AttributeType getAttribute()
  {
    return attribute;
  }
}
