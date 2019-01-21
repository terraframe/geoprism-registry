package net.geoprism.georegistry.io;

import java.util.Collection;

import org.commongeoregistry.adapter.Term;

public class GeoObjectUtil
{
  @SuppressWarnings("unchecked")
  public static String convertToTermString(Object value)
  {
    Collection<Term> terms = (Collection<Term>) value;
    StringBuilder builder = new StringBuilder();

    boolean first = true;

    for (Term term : terms)
    {
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
