package net.geoprism.registry.query.postgres;

import org.commongeoregistry.adapter.constants.DefaultAttribute;

import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.constants.ComponentInfo;
import com.runwaysdk.constants.EnumerationMasterInfo;
import com.runwaysdk.query.AttributeLocal;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.query.ValueQuery;

import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.query.ServerGeoObjectQuery;

public class LeafGeoObjectQuery extends AbstractGeoObjectQuery implements ServerGeoObjectQuery
{

  public LeafGeoObjectQuery(ServerGeoObjectType type)
  {
    super(type);
  }

  public ValueQuery getValueQuery()
  {
    QueryFactory factory = new QueryFactory();
    ValueQuery vQuery = new ValueQuery(factory);

    BusinessQuery bQuery = new BusinessQuery(vQuery, this.getType().definesType());

    configureQuery(vQuery, bQuery);

    if (this.getLimit() != null)
    {
      vQuery.restrictRows(this.getLimit(), 1);
    }
    return vQuery;
  }

  protected void configureQuery(ValueQuery vQuery, BusinessQuery bQuery)
  {
    AttributeLocal label = bQuery.aLocalCharacter(DefaultAttribute.DISPLAY_LABEL.getName());

    vQuery.SELECT(bQuery.aUUID(ComponentInfo.OID, ComponentInfo.OID));
    vQuery.SELECT(bQuery.aUUID(RegistryConstants.UUID, RegistryConstants.UUID));
    vQuery.SELECT(bQuery.aCharacter(DefaultAttribute.CODE.getName()));
    vQuery.SELECT(bQuery.aEnumeration(DefaultAttribute.STATUS.getName()).aCharacter(EnumerationMasterInfo.NAME, DefaultAttribute.STATUS.getName()));
    vQuery.SELECT(bQuery.get(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME, RegistryConstants.GEOMETRY_ATTRIBUTE_NAME));

    this.selectLabelAttribute(vQuery, label);

    this.selectCustomAttributes(vQuery, bQuery);

    if (this.getRestriction() != null)
    {
      this.getRestriction().create(this).restrict(vQuery, bQuery);
    }

    vQuery.ORDER_BY_ASC(bQuery.aCharacter(DefaultAttribute.CODE.getName()));
  }
}
