/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Common Geo Registry Adapter(tm).
 *
 * Common Geo Registry Adapter(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Common Geo Registry Adapter(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Common Geo Registry Adapter(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package org.commongeoregistry.adapter;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.CustomSerializer;
import org.commongeoregistry.adapter.metadata.DefaultSerializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Term implements Serializable
{
  /**
   * 
   */
  private static final long  serialVersionUID           = 8658638930185089125L;

  public static final String JSON_CODE                  = "code";

  public static final String JSON_LOCALIZED_LABEL       = "label";

  public static final String JSON_LOCALIZED_DESCRIPTION = "description";

  public static final String JSON_CHILDREN              = "children";

  private String             code;

  private LocalizedValue     label;

  private LocalizedValue     description;

  private List<Term>         children;

  public Term(String code, LocalizedValue label, LocalizedValue description)
  {
    this.code = code;
    this.label = label;
    this.description = description;

    this.children = Collections.synchronizedList(new LinkedList<Term>());
  }

  public String getCode()
  {
    return this.code;
  }

  public LocalizedValue getLabel()
  {
    return this.label;
  }

  public LocalizedValue getDescription()
  {
    return this.description;
  }

  public void addChild(Term childTerm)
  {
    this.children.add(childTerm);
  }

  public List<Term> getChildren()
  {
    return this.children;
  }

  public static JsonArray toJSON(Term[] terms)
  {
    JsonArray json = new JsonArray();
    for (Term term : terms)
    {
      json.add(term.toJSON());
    }

    return json;
  }

  public JsonObject toJSON()
  {
    return toJSON(new DefaultSerializer());
  }

  public JsonObject toJSON(CustomSerializer serializer)
  {
    JsonObject obj = new JsonObject();
    obj.addProperty(JSON_CODE, this.getCode());
    obj.add(JSON_LOCALIZED_LABEL, this.getLabel().toJSON(serializer));
    obj.add(JSON_LOCALIZED_DESCRIPTION, this.getDescription().toJSON(serializer));

    // Child Terms are not stored in a hierarchy structure. They are flattened
    // in an array.
    JsonArray childTerms = new JsonArray();
    for (int i = 0; i < this.getChildren().size(); i++)
    {
      Term child = this.getChildren().get(i);
      childTerms.add(child.toJSON());
    }
    obj.add(JSON_CHILDREN, childTerms);

    return obj;
  }

  /**
   * Creates a {@link Term} object including references to child terms.
   * 
   * @param termObj
   * @return
   */
  public static Term fromJSON(JsonObject termObj)
  {
    if (!termObj.get(Term.JSON_CODE).isJsonNull())
    {
      String code = termObj.get(Term.JSON_CODE).getAsString();
      LocalizedValue label = LocalizedValue.fromJSON(termObj.get(Term.JSON_LOCALIZED_LABEL).getAsJsonObject());

      LocalizedValue description = new LocalizedValue(null);

      if (termObj.has(Term.JSON_LOCALIZED_DESCRIPTION) && !termObj.get(Term.JSON_LOCALIZED_DESCRIPTION).isJsonNull())
      {
        description = LocalizedValue.fromJSON(termObj.get(Term.JSON_LOCALIZED_DESCRIPTION).getAsJsonObject());
      }

      Term term = new Term(code, label, description);

      if (termObj.has(Term.JSON_CHILDREN))
      {
        JsonElement children = termObj.get(Term.JSON_CHILDREN);

        if (children != null && !children.isJsonNull() && children.isJsonArray())
        {
          JsonArray childrenArray = children.getAsJsonArray();

          for (JsonElement jsonElement : childrenArray)
          {
            if (jsonElement.isJsonObject())
            {
              JsonObject childTermObj = jsonElement.getAsJsonObject();

              Term childTerm = Term.fromJSON(childTermObj);
              term.addChild(childTerm);
            }
          }
        }
      }

      return term;
    }

    return null;
  }

  @Override
  public boolean equals(Object obj)
  {
    return ( obj instanceof Term ) && ( (Term) obj ).getCode().equals(this.getCode());
  }

  @Override
  public String toString()
  {
    String toString = "Term: " + this.code + " ";

    boolean firstIteration = true;
    for (Term child : getChildren())
    {
      if (firstIteration)
      {
        toString += "Children:{";
        firstIteration = false;
      }
      else
      {
        toString += ", ";
      }

      toString += child.toString();
    }

    if (!firstIteration)
    {
      toString += "}";
    }

    return toString;
  }
}
