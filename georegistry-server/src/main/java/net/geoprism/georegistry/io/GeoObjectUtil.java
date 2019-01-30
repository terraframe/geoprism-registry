package net.geoprism.georegistry.io;

import java.util.Iterator;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.metadata.AttributeTermType;

public class GeoObjectUtil
{
  @SuppressWarnings("unchecked")
  public static String convertToTermString(AttributeTermType attributeType, Object value)
  {
    Iterator<String> codes = (Iterator<String>) value;
    StringBuilder builder = new StringBuilder();

    boolean first = true;

    while (codes.hasNext())
    {
      String code = codes.next();
      Term term = attributeType.getTermByCode(code).get();

      if (!first)
      {
        builder.append(",");
      }

      builder.append(term.getLocalizedLabel());
      first = false;
    }

    return builder.toString();
  }
}
