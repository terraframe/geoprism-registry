package net.geoprism.registry.graph;

import org.commongeoregistry.adapter.metadata.FrequencyType;

import com.runwaysdk.constants.MdAttributeBooleanInfo;
import com.runwaysdk.gis.constants.MdGeoVertexInfo;
import com.runwaysdk.gis.dataaccess.MdGeoVertexDAOIF;
import com.runwaysdk.gis.dataaccess.metadata.graph.MdGeoVertexDAO;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.graph.ChangeFrequency;

import net.geoprism.registry.RegistryConstants;

public class GeoVertexType extends GeoVertexTypeBase
{
  private static final long serialVersionUID = 224165207;

  public GeoVertexType()
  {
    super();
  }

  public static MdGeoVertexDAO create(String code, FrequencyType frequency)
  {
    ChangeFrequency cFrequency = ChangeFrequency.valueOf(frequency.name());

    MdGeoVertexDAOIF mdGeoVertexDAO = MdGeoVertexDAO.getMdGeoVertexDAO(GeoVertex.CLASS);

    MdGeoVertexDAO child = MdGeoVertexDAO.newInstance();
    child.setValue(MdGeoVertexInfo.PACKAGE, RegistryConstants.UNIVERSAL_GRAPH_PACKAGE);
    child.setValue(MdGeoVertexInfo.NAME, code);
    child.setValue(MdGeoVertexInfo.SUPER_MD_VERTEX, mdGeoVertexDAO.getOid());
    child.setValue(MdGeoVertexInfo.ENABLE_CHANGE_OVER_TIME, MdAttributeBooleanInfo.TRUE);
    child.setValue(MdGeoVertexInfo.GENERATE_SOURCE, MdAttributeBooleanInfo.FALSE);
    child.addItem(MdGeoVertexInfo.FREQUENCY, cFrequency.getOid());
    child.apply();

    return child;
  }

  public static void remove(String code)
  {
    MdGeoVertexDAO mdGeoVertex = getMdGeoVertex(code);
    mdGeoVertex.delete();
  }

  public static MdGeoVertexDAO getMdGeoVertex(String code)
  {
    if (!code.equals(Universal.ROOT))
    {
      return MdGeoVertexDAO.getMdGeoVertexDAO(RegistryConstants.UNIVERSAL_GRAPH_PACKAGE + "." + code).getBusinessDAO();
    }

    return null;
  }
}
