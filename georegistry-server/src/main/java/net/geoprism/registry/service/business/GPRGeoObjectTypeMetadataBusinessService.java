package net.geoprism.registry.service.business;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.gis.geo.Universal;

import net.geoprism.registry.business.GeoObjectTypeMetadataBusinessService;
import net.geoprism.registry.business.GeoObjectTypeMetadataBusinessServiceIF;

import net.geoprism.registry.ListType;
import net.geoprism.registry.ListTypeQuery;
import net.geoprism.registry.ListTypeVersionQuery;
import net.geoprism.registry.geoobjecttype.PrivateTypeHasPublicChildren;
import net.geoprism.registry.geoobjecttype.PrivateTypeIsReferencedInPublicMasterLists;
import net.geoprism.registry.geoobjecttype.TypeHasPrivateParents;
import net.geoprism.registry.model.GeoObjectTypeMetadata;
import net.geoprism.registry.model.ServerGeoObjectType;

@Service
@Primary
public class GPRGeoObjectTypeMetadataBusinessService extends GeoObjectTypeMetadataBusinessService implements GeoObjectTypeMetadataBusinessServiceIF
{
  @Override
  public void apply(GeoObjectTypeMetadata gotm)
  {
    if (!gotm.isNew() && gotm.isModified(GeoObjectTypeMetadata.ISPRIVATE))
    {
      final ServerGeoObjectType type = gotm.getServerType();

      // They aren't allowed to set this to private in certain scenarios
      if (gotm.getIsPrivate())
      {
        if (hasPublicChildren(gotm))
        {
          PrivateTypeHasPublicChildren ex = new PrivateTypeHasPublicChildren();
          ex.setTypeLabel(gotm.getServerType().getLabel().getValue());
          throw ex;
        }

        if (isReferencedInPublicListTypes(gotm))
        {
          PrivateTypeIsReferencedInPublicMasterLists ex = new PrivateTypeIsReferencedInPublicMasterLists();
          ex.setTypeLabel(gotm.getServerType().getLabel().getValue());
          throw ex;
        }
      }
      else
      {
        if (hasPrivateParents(gotm))
        {
          TypeHasPrivateParents ex = new TypeHasPrivateParents();
          ex.setTypeLabel(gotm.getServerType().getLabel().getValue());
          throw ex;
        }
      }

      // Set the isPrivate field for all children
      if (gotm.isModified(GeoObjectTypeMetadata.ISPRIVATE) && type.getIsAbstract())
      {
        List<ServerGeoObjectType> subtypes = gotService.getSubtypes(type);

        for (ServerGeoObjectType subtype : subtypes)
        {
          GeoObjectTypeMetadata submetadata = subtype.getMetadata();

          submetadata.appLock();
          submetadata.setIsPrivate(gotm.getIsPrivate());
          submetadata.apply();
        }
      }
    }

    gotm.apply();
  }

  private boolean isReferencedInPublicListTypes(GeoObjectTypeMetadata gotm)
  {
    Universal uni = gotm.getUniversal();
    ServerGeoObjectType type = gotm.getServerType();

    QueryFactory qf = new QueryFactory();

    ListTypeVersionQuery versionQuery = new ListTypeVersionQuery(qf);
    versionQuery.WHERE(versionQuery.getListVisibility().EQ(ListType.PUBLIC));
    versionQuery.OR(versionQuery.getGeospatialVisibility().EQ(ListType.PUBLIC));

    ListTypeQuery mlq = new ListTypeQuery(qf);
    mlq.WHERE(mlq.EQ(versionQuery.getListType()));

    OIterator<? extends ListType> it = mlq.getIterator();

    while (it.hasNext())
    {
      ListType list = it.next();

      if (list.getUniversal().getOid().equals(uni.getOid()))
      {
        PrivateTypeIsReferencedInPublicMasterLists ex = new PrivateTypeIsReferencedInPublicMasterLists();
        ex.setTypeLabel(gotm.getServerType().getLabel().getValue());
        throw ex;
      }
      else
      {
        JsonArray hierarchies = list.getHierarchiesAsJson();

        for (int i = 0; i < hierarchies.size(); i++)
        {
          JsonObject hierarchy = hierarchies.get(i).getAsJsonObject();

          JsonArray parents = hierarchy.get("parents").getAsJsonArray();

          for (int j = 0; j < parents.size(); ++j)
          {
            JsonObject parent = parents.get(j).getAsJsonObject();

            if (parent.has("selected") && parent.get("selected").getAsBoolean())
            {
              if (parent.has("code") && parent.get("code").getAsString().equals(type.getCode()))
              {
                return true;
              }
            }
          }
        }
      }
    }

    return false;
  }
}
