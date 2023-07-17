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
package net.geoprism.registry.etl.upload;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.runwaysdk.business.graph.VertexObject;

import net.geoprism.registry.model.ServerGeoObjectIF;

public class ClassifierCache
{
  @SuppressWarnings("serial")
  protected class LinkedHashMapCache<a, b> extends LinkedHashMap<a, b>
  {
    protected LinkedHashMapCache()
    {
      super(cacheSize + 1, .75F, true);
    }

    protected boolean removeEldestEntry(@SuppressWarnings("rawtypes") Map.Entry eldest)
    {
      return size() > cacheSize;
    }
  };

  protected Integer                                cacheSize                          = 10000;

  protected Map<String, Map<String, VertexObject>> classifierCache                    = Collections.synchronizedMap(new HashMap<String, Map<String, VertexObject>>());

  protected Map<String, Map<String, Boolean>>      classifierAttributeValidationCache = Collections.synchronizedMap(new HashMap<String, Map<String, Boolean>>());

  public ClassifierCache()
  {

  }

  public ClassifierCache(Integer cacheSize)
  {
    this.cacheSize = cacheSize;
  }

  public VertexObject getClassifier(String classificationType, String code)
  {
    if (!this.classifierCache.containsKey(classificationType))
    {
      this.classifierCache.put(classificationType, new LinkedHashMapCache<String, VertexObject>());
    }

    return this.classifierCache.get(classificationType).get(code);
  }

  public void putClassifier(String classificationType, String code, VertexObject classifier)
  {
    if (!this.classifierCache.containsKey(classificationType))
    {
      this.classifierCache.put(classificationType, new LinkedHashMap<String, VertexObject>());
    }

    this.classifierCache.get(classificationType).put(code, classifier);
  }

  public Boolean getClassifierAttributeValidation(String attributeId, VertexObject classifier)
  {
    if (!this.classifierAttributeValidationCache.containsKey(attributeId))
    {
      this.classifierAttributeValidationCache.put(attributeId, new LinkedHashMap<String, Boolean>());
    }

    return this.classifierAttributeValidationCache.get(attributeId).get(classifier.getOid());
  }

  public void putClassifierAttributeValidation(String attributeId, VertexObject classifier, Boolean validationResult)
  {
    if (!this.classifierAttributeValidationCache.containsKey(attributeId))
    {
      this.classifierAttributeValidationCache.put(attributeId, new LinkedHashMap<String, Boolean>());
    }

    this.classifierAttributeValidationCache.get(attributeId).put(classifier.getOid(), validationResult);
  }
}
