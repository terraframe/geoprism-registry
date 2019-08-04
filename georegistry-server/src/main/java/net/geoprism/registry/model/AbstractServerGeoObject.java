package net.geoprism.registry.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.ArrayUtils;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;

import com.runwaysdk.business.ontology.TermAndRel;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.metadata.MdTermRelationship;
import com.runwaysdk.system.ontology.TermUtil;

import net.geoprism.registry.AttributeHierarchy;
import net.geoprism.registry.conversion.ServerGeoObjectFactory;
import net.geoprism.registry.conversion.ServerHierarchyTypeBuilder;

public abstract class AbstractServerGeoObject
{

  public Map<String, ServerHierarchyType> getHierarchyTypeMap(String[] relationshipTypes)
  {
    Map<String, ServerHierarchyType> map = new HashMap<String, ServerHierarchyType>();

    for (String relationshipType : relationshipTypes)
    {
      MdTermRelationship mdRel = (MdTermRelationship) MdTermRelationship.getMdRelationship(relationshipType);

      ServerHierarchyType ht = new ServerHierarchyTypeBuilder().get(mdRel);

      map.put(relationshipType, ht);
    }

    return map;
  }

  protected static ParentTreeNode internalGetParentGeoObjects(ServerGeoObjectIF child, String[] parentTypes, boolean recursive, ServerHierarchyType htIn)
  {
    ParentTreeNode tnRoot = new ParentTreeNode(child.getGeoObject(), htIn != null ? htIn.getType() : null);

    if (child.getType().isLeaf())
    {
      List<MdAttributeDAOIF> mdAttributes = child.getMdAttributeDAOs().stream().filter(mdAttribute -> {
        if (mdAttribute instanceof MdAttributeReferenceDAOIF)
        {
          MdBusinessDAOIF referenceMdBusiness = ( (MdAttributeReferenceDAOIF) mdAttribute ).getReferenceMdBusinessDAO();

          if (referenceMdBusiness.definesType().equals(GeoEntity.CLASS))
          {
            return true;
          }
        }

        return false;
      }).collect(Collectors.toList());

      mdAttributes.forEach(mdAttribute -> {

        String parentRunwayId = child.getValue(mdAttribute.definesAttribute());

        if (parentRunwayId != null && parentRunwayId.length() > 0)
        {
          GeoEntity geParent = GeoEntity.get(parentRunwayId);
          ServerGeoObjectIF parent = ServerGeoObjectFactory.build(geParent);
          Universal uni = parent.getType().getUniversal();

          if (parentTypes == null || parentTypes.length == 0 || ArrayUtils.contains(parentTypes, uni.getKey()))
          {
            ParentTreeNode tnParent;

            ServerHierarchyType ht = AttributeHierarchy.getHierarchyType(mdAttribute.getKey());

            if (recursive)
            {
              tnParent = AbstractServerGeoObject.internalGetParentGeoObjects(parent, parentTypes, recursive, ht);
            }
            else
            {
              tnParent = new ParentTreeNode(parent.getGeoObject(), ht.getType());
            }

            tnRoot.addParent(tnParent);
          }
        }
      });

    }
    else
    {
      String[] relationshipTypes = TermUtil.getAllChildRelationships(child.getRunwayId());

      Map<String, ServerHierarchyType> htMap = child.getHierarchyTypeMap(relationshipTypes);

      TermAndRel[] tnrParents = TermUtil.getDirectAncestors(child.getRunwayId(), relationshipTypes);
      for (TermAndRel tnrParent : tnrParents)
      {
        GeoEntity geParent = (GeoEntity) tnrParent.getTerm();
        Universal uni = geParent.getUniversal();

        if (!geParent.getOid().equals(GeoEntity.getRoot().getOid()) && ( parentTypes == null || parentTypes.length == 0 || ArrayUtils.contains(parentTypes, uni.getKey()) ))
        {
          ServerGeoObjectIF parent = ServerGeoObjectFactory.build(geParent);
          ServerHierarchyType ht = htMap.get(tnrParent.getRelationshipType());

          ParentTreeNode tnParent;
          if (recursive)
          {
            tnParent = AbstractServerGeoObject.internalGetParentGeoObjects(parent, parentTypes, recursive, ht);
          }
          else
          {
            tnParent = new ParentTreeNode(parent.getGeoObject(), ht.getType());
          }

          tnRoot.addParent(tnParent);
        }
      }
    }

    return tnRoot;
  }

}
