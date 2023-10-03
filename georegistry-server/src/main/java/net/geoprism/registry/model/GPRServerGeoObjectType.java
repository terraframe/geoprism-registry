package net.geoprism.registry.model;

import org.commongeoregistry.adapter.Optional;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.RegistryRole;

import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.metadata.MdAttributeConcrete;
import com.runwaysdk.system.metadata.MdBusiness;

import net.geoprism.registry.ListType;
import net.geoprism.registry.service.ChangeRequestService;
import net.geoprism.registry.service.SearchService;
import net.geoprism.registry.service.SerializedListTypeCache;
import net.geoprism.registry.ws.GlobalNotificationMessage;
import net.geoprism.registry.ws.MessageType;
import net.geoprism.registry.ws.NotificationFacade;

public class GPRServerGeoObjectType extends ServerGeoObjectType
{

  public GPRServerGeoObjectType(GeoObjectType go, Universal universal, MdBusiness mdBusiness, MdVertexDAOIF mdVertex)
  {
    super(go, universal, mdBusiness, mdVertex);
  }
  
  @Override
  public void delete()
  {
    super.delete();

    SerializedListTypeCache.getInstance().clear();

    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.TYPE_CACHE_CHANGE, null));
  }
  
  @Override
  @Transaction
  protected void deleteInTransaction()
  {
    super.deleteInTransaction();
    
    ListType.markAllAsInvalid(null, this);

    new SearchService().clear(this.getCode());

    new ChangeRequestService().markAllAsInvalid(this);
  }
  
  @Override
  public void update(GeoObjectType geoObjectTypeNew)
  {
    super.update(geoObjectTypeNew);
    
    SerializedListTypeCache.getInstance().clear();
  }
  
  @Transaction
  @Override
  public MdAttributeConcrete createMdAttributeFromAttributeType(AttributeType attributeType)
  {
    MdAttributeConcrete mdAttribute = super.createMdAttributeFromAttributeType(attributeType);

    ListType.createMdAttribute(this, attributeType);

    return mdAttribute;
  }
  
  @Transaction
  @Override
  public void deleteMdAttributeFromAttributeType(String attributeName)
  {
    Optional<AttributeType> optional = this.type.getAttribute(attributeName);
    
    super.deleteMdAttributeFromAttributeType(attributeName);

    if (optional.isPresent())
    {
      ListType.deleteMdAttribute(this.universal, optional.get());
    }
  }
  
  public String getMaintainerRoleName()
  {
    ServerGeoObjectType superType = this.getSuperType();

    if (superType != null)
    {
      return superType.getMaintainerRoleName();
    }

    return RegistryRole.Type.getRM_RoleName(this.getOrganization().getCode(), this.getCode());
  }

  public String getAdminRoleName()
  {
    ServerGeoObjectType superType = this.getSuperType();

    if (superType != null)
    {
      return superType.getOrganization().getRegistryAdminRoleName();
    }

    return this.getOrganization().getRegistryAdminRoleName();
  }

}
