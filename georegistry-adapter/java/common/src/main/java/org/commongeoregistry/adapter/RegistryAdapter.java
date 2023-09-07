/**
 *
 */
package org.commongeoregistry.adapter;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.Attribute;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.commongeoregistry.adapter.dataaccess.ValueOverTimeCollectionDTO;
import org.commongeoregistry.adapter.id.AdapterIdServiceIF;
import org.commongeoregistry.adapter.id.EmptyIdCacheException;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.MetadataCache;

public abstract class RegistryAdapter implements Serializable
{
  /**
   * 
   */
  private static final long  serialVersionUID = -5085432383838987882L;

  private MetadataCache      metadataCache;

  private AdapterIdServiceIF idService;

  public RegistryAdapter(AdapterIdServiceIF idService)
  {
    this.metadataCache = new MetadataCache(this);
    this.metadataCache.rebuild();
    this.idService = idService;
  }

  public MetadataCache getMetadataCache()
  {
    return this.metadataCache;
  }

  public AdapterIdServiceIF getIdService()
  {
    return this.idService;
  }

  /**
   * Creates a new local {@link GeoObject} instance of the given type. If the
   * local id cache is empty, an EmptyIdCacheException is thrown.
   * 
   * @param geoObjectTypeCode
   * @return a new local {@link GeoObject} instance of the given type.
   */
  public GeoObject newGeoObjectInstance(String geoObjectTypeCode) throws EmptyIdCacheException
  {
    return newGeoObjectInstance(geoObjectTypeCode, true);
  }

  /**
   * Creates a new local {@link GeoObject} instance of the given type. If genId is true and the
   * local id cache is empty, an EmptyIdCacheException is thrown.
   * 
   * @param geoObjectTypeCode
   * @param genId Whether or not this GeoObject should be given a new id from the id cache.
   * @return a new local {@link GeoObject} instance of the given type.
   */
  public GeoObject newGeoObjectInstance(String geoObjectTypeCode, boolean genId) throws EmptyIdCacheException
  {
    Optional<GeoObjectType> opGOT = this.getMetadataCache().getGeoObjectType(geoObjectTypeCode);
    
    if (!opGOT.isPresent())
    {
      throw new GeoObjectTypeNotFoundException(geoObjectTypeCode);
    }
    
    GeoObjectType geoObjectType = opGOT.get();

    Map<String, Attribute> attributeMap = GeoObject.buildAttributeMap(geoObjectType);

    GeoObject geoObject = new GeoObject(geoObjectType, geoObjectType.getGeometryType(), attributeMap);

    // Set some default values
    // geoObject.getAttribute(DefaultAttribute.TYPE.getName()).setValue(geoObjectTypeCode);

    geoObject.getAttribute(DefaultAttribute.EXISTS.getName()).setValue(true);
    
    geoObject.getAttribute(DefaultAttribute.INVALID.getName()).setValue(false);

    if (genId)
    {
      String uid = this.idService.next();
      geoObject.setUid(uid);
    }

    return geoObject;
  }
  
  /**
   * Creates a new local {@link GeoObjectOverTime} instance of the given type. If the
   * local id cache is empty, an EmptyIdCacheException is thrown.
   * 
   * @param geoObjectTypeCode
   * @return a new local {@link GeoObjectOverTime} instance of the given type.
   */
  public GeoObjectOverTime newGeoObjectOverTimeInstance(String geoObjectTypeCode) throws EmptyIdCacheException
  {
    return newGeoObjectOverTimeInstance(geoObjectTypeCode, true);
  }
  
  /**
   * Creates a new local {@link GeoObjectOverTime} instance of the given type. If genId is true and the
   * local id cache is empty, an EmptyIdCacheException is thrown.
   * 
   * @param geoObjectTypeCode
   * @param genId Whether or not this {@link GeoObjectOverTime} should be given a new id from the id cache.
   * @return a new local {@link GeoObjectOverTime} instance of the given type.
   */
  public GeoObjectOverTime newGeoObjectOverTimeInstance(String geoObjectTypeCode, boolean genId) throws EmptyIdCacheException
  {
    final Date createDate = new Date();
    
    GeoObjectType geoObjectType = this.getMetadataCache().getGeoObjectType(geoObjectTypeCode).get();

    Map<String, ValueOverTimeCollectionDTO> votAttributeMap = GeoObjectOverTime.buildVotAttributeMap(geoObjectType);
    Map<String, Attribute> attributeMap = GeoObjectOverTime.buildAttributeMap(geoObjectType);

    GeoObjectOverTime geoObject = new GeoObjectOverTime(geoObjectType, votAttributeMap, attributeMap);

    // Set some default values
    // geoObject.getAttribute(DefaultAttribute.TYPE.getName()).setValue(geoObjectTypeCode);
    
    geoObject.setInvalid(false);

    if (genId)
    {
      String uid = this.idService.next();
      geoObject.setUid(uid);
    }

    return geoObject;
  }
}
