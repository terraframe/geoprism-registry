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
package org.commongeoregistry.adapter.metadata;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.commongeoregistry.adapter.Optional;
import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.dataaccess.UnknownTermException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class AttributeTermType extends AttributeType
{
  /**
   * 
   */
  private static final long  serialVersionUID = 6431580798592645011L;

  public static final String JSON_ROOT_TERM   = "rootTerm";

  public static String       TYPE             = "term";

  private Term               rootTerm         = null;

  private Map<String, Term>  termMap          = null;

  public AttributeTermType(String _name, LocalizedValue _label, LocalizedValue _description, boolean _isDefault, boolean _required, boolean _unique)
  {
    super(_name, _label, _description, TYPE, _isDefault, _required, _unique);

    this.termMap = new ConcurrentHashMap<String, Term>();
  }

  public Term getRootTerm()
  {
    return this.rootTerm;
  }

  public void setRootTerm(Term rootTerm)
  {
    this.termMap.clear();

    this.rootTerm = rootTerm;

    this.buildTermMap(rootTerm);
  }

  private void buildTermMap(Term term)
  {
    this.termMap.put(term.getCode(), term);

    for (Term childTerm : term.getChildren())
    {
      this.buildTermMap(childTerm);
    }
  }

  public List<Term> getTerms()
  {
    return this.rootTerm.getChildren();
  }

  public Optional<Term> getTermByCode(String termCode)
  {
    Term term = this.termMap.get(termCode);

    return Optional.of(term);
  }

  @Override
  public JsonObject toJSON(CustomSerializer serializer)
  {
    JsonObject json = super.toJSON(serializer);

    if (this.rootTerm != null)
    {
      json.add(JSON_ROOT_TERM, this.getRootTerm().toJSON());
    }

    return json;
  }

  /**
   * Populates any additional attributes from JSON that were not populated in
   * {@link GeoObjectType#fromJSON(String, org.commongeoregistry.adapter.RegistryAdapter)}
   * 
   * @param attrObj
   * @return {@link AttributeType}
   */
  @Override
  public void fromJSON(JsonObject attrObj)
  {
    super.fromJSON(attrObj);

    JsonElement termElement = attrObj.get(AttributeTermType.JSON_ROOT_TERM);

    if (termElement != null && !termElement.isJsonNull())
    {
      Term rootTerm = Term.fromJSON(termElement.getAsJsonObject());

      if (rootTerm != null)
      {
        this.setRootTerm(rootTerm);
      }
    }
  }

  @Override
  public void validate(Object _value)
  {
    if (_value instanceof Term)
    {
      this.validate( ( (Term) _value ).getCode());
    }
    else
    {
      this.validate((String) _value);
    }
  }

  public void validate(String code)
  {
    if (!this.termMap.containsKey(code))
    {
      throw new UnknownTermException(code, this);
    }
  }
}
