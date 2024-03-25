package net.geoprism.registry.tile;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Set;

import org.commongeoregistry.adapter.constants.DefaultAttribute;

import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.database.Database;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.localization.LocalizationFacade;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.mapping.GeoserverFacade;

import net.geoprism.registry.ListTypeVersion;
import net.geoprism.registry.MasterListVersion;
import net.geoprism.registry.RegistryConstants;

public class PostgisVectorTileBuilder
{
  private Set<Locale>     locales;

  private String          column;

  private String          labelColumn;

  private MdBusinessDAOIF mdBusiness;

  public PostgisVectorTileBuilder(ListTypeVersion version)
  {
    this.mdBusiness = MdBusinessDAO.get(version.getMdBusinessOid());

    MdAttributeConcreteDAOIF geomAttribute = mdBusiness.definesAttribute(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME);
    MdAttributeConcreteDAOIF labelAttribute = mdBusiness.definesAttribute(DefaultAttribute.DISPLAY_LABEL.getName() + MasterListVersion.DEFAULT_LOCALE);

    this.locales = LocalizationFacade.getInstalledLocales();
    this.column = geomAttribute.getColumnName();
    this.labelColumn = labelAttribute.getColumnName();
  }

  public byte[] write(int zoom, int x, int y)
  {
    StringBuilder statement = new StringBuilder();
    statement.append("WITH mvtgeom AS (" + "\n");
    statement.append(" SELECT " + "\n");
    statement.append("  ge.oid AS " + GeoEntity.OID + "\n");
    statement.append(", ge.uid AS " + DefaultAttribute.UID.getName() + "\n");
    statement.append(", ge.code AS" + DefaultAttribute.CODE.getName() + "\n");
    statement.append(", ge." + labelColumn + " AS label" + "\n");

    for (Locale locale : locales)
    {
      MdAttributeConcreteDAOIF localeAttribute = mdBusiness.definesAttribute(DefaultAttribute.DISPLAY_LABEL.getName() + locale.toString());

      if (localeAttribute != null)
      {
        statement.append(", ge." + localeAttribute.getColumnName() + " AS label_" + locale.toString().toLowerCase() + "\n");
      }
    }
    statement.append(", ST_AsMVTGeom(" + "\n");
    statement.append("    ST_Transform( ge." + column + ", 3857 )" + "\n");
    statement.append("    , ST_TileEnvelope(" + zoom + ", " + x + ", " + y + ")" + "\n");
    statement.append("    , extent => 4096" + "\n");
    statement.append("    , buffer => 64" + "\n");
    statement.append("  ) AS " + GeoserverFacade.GEOM_COLUMN + "\n");
    statement.append(" FROM " + mdBusiness.getTableName() + " AS ge" + "\n");
    statement.append(" WHERE ST_Transform( ge." + column + ", 3857 ) && ST_TileEnvelope(" + zoom + ", " + x + ", " + y + ", margin => (64.0 / 4096))" + "\n");
    statement.append(")" + "\n");
    statement.append("SELECT ST_AsMVT(mvtgeom.*, 'context')" + "\n");
    statement.append("FROM mvtgeom;" + "\n");

    try (ResultSet result = Database.query(statement.toString()))
    {
      if (result.next())
      {
        return result.getBytes(1);
      }
    }
    catch (SQLException e)
    {
      e.printStackTrace();
    }

    return new byte[] {};

  }
}
