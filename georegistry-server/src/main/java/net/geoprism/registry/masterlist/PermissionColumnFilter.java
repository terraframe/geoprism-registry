package net.geoprism.registry.masterlist;

import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.metadata.MdAttributeConcreteDAO;

import net.geoprism.registry.ListType;
import net.geoprism.registry.ListTypeAttribute;
import net.geoprism.registry.ListTypeVersion;
import net.geoprism.registry.Organization;

public class PermissionColumnFilter implements ColumnFilter
{
  private boolean isPrivate;

  public PermissionColumnFilter(ListTypeVersion version)
  {
    this.isPrivate = version.getListVisibility().equals(ListType.PRIVATE) && !Organization.isMember(version.getListType().getOrganization());
  }

  @Override
  public boolean isValid(ListTypeAttribute attribute)
  {
    if (this.isPrivate)
    {
      MdAttributeConcreteDAOIF mdAttribute = MdAttributeConcreteDAO.get(attribute.getListAttributeOid());
      String attributeName = mdAttribute.definesAttribute();

      return attributeName.equals("code") || attributeName.contains("displayLabel");
    }

    return true;
  }

  @Override
  public boolean isValid(ListColumn column)
  {
    if (column instanceof ListAttributeGroup)
    {
      return ( (ListAttributeGroup) column ).getColumns().size() > 0;
    }

    return true;
  }

}
