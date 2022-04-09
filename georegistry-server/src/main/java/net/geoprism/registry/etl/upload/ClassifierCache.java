package net.geoprism.registry.etl.upload;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.runwaysdk.business.graph.VertexObject;

public class ClassifierCache
{
  protected Integer cacheSize = 10000;
  
  protected Map<String, Map<String, VertexObject>> classifierCache = new HashMap<String, Map<String, VertexObject>>();
  
  protected Map<String, Map<String, Boolean>> classifierAttributeValidationCache = new HashMap<String, Map<String, Boolean>>();
  
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
      this.classifierCache.put(classificationType, new LinkedHashMap<String, VertexObject>(this.cacheSize + 1, .75F, true));
    }
    
    return this.classifierCache.get(classificationType).get(code);
  }
  
  public void putClassifier(String classificationType, String code, VertexObject classifier)
  {
    if (!this.classifierCache.containsKey(classificationType))
    {
      this.classifierCache.put(classificationType, new LinkedHashMap<String, VertexObject>(this.cacheSize + 1, .75F, true));
    }
    
    this.classifierCache.get(classificationType).put(code, classifier);
  }
  
  public Boolean getClassifierAttributeValidation(String attributeId, VertexObject classifier)
  {
    if (!this.classifierAttributeValidationCache.containsKey(attributeId))
    {
      this.classifierAttributeValidationCache.put(attributeId, new LinkedHashMap<String, Boolean>(this.cacheSize + 1, .75F, true));
    }
    
    return this.classifierAttributeValidationCache.get(attributeId).get(classifier.getOid());
  }
  
  public void putClassifierAttributeValidation(String attributeId, VertexObject classifier, Boolean validationResult)
  {
    if (!this.classifierAttributeValidationCache.containsKey(attributeId))
    {
      this.classifierAttributeValidationCache.put(attributeId, new LinkedHashMap<String, Boolean>(this.cacheSize + 1, .75F, true));
    }
    
    this.classifierAttributeValidationCache.get(attributeId).put(classifier.getOid(), validationResult);
  }
}
