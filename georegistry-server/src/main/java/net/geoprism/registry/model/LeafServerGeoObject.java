package net.geoprism.registry.model;

import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.dataaccess.ChildTreeNode;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.business.Business;
import com.runwaysdk.business.LocalStruct;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.database.Database;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.Universal;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import net.geoprism.dashboard.GeometryUpdateException;
import net.geoprism.registry.GeometryTypeException;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.conversion.LocalizedValueConverter;

public class LeafServerGeoObject extends AbstractServerGeoObject implements ServerGeoObjectIF
{
  private Logger logger = LoggerFactory.getLogger(LeafServerGeoObject.class);

  public LeafServerGeoObject(ServerGeoObjectType type, GeoObject go, Business business)
  {
    super(type, go, business);
  }

  @Override
  public String getCode()
  {
    return this.getGeoObject().getCode();
  }

  @Override
  public String getUid()
  {
    return this.getGeoObject().getUid();
  }

  @Override
  public String getRunwayId()
  {
    return this.getBusiness().getOid();
  }

  @Override
  public List<? extends MdAttributeConcreteDAOIF> getMdAttributeDAOs()
  {
    return this.getBusiness().getMdAttributeDAOs();
  }

  @Override
  public String getValue(String attributeName)
  {
    return this.getBusiness().getValue(attributeName);
  }

  @Override
  public ChildTreeNode getChildGeoObjects(String[] childrenTypes, Boolean recursive)
  {
    throw new UnsupportedOperationException("Leaf nodes cannot have children.");
  }

  @Override
  public ParentTreeNode getParentGeoObjects(String[] parentTypes, Boolean recursive)
  {
    return AbstractServerGeoObject.internalGetParentGeoObjects(this, parentTypes, recursive, null);
  }

  @Override
  public void removeChild(ServerGeoObjectIF child, String hierarchyCode)
  {
    throw new UnsupportedOperationException("Virtual leaf nodes cannot have children.");
  }

  @Override
  public ParentTreeNode addChild(ServerGeoObjectIF child, String hierarchyCode)
  {
    throw new UnsupportedOperationException("Virtual leaf nodes cannot have children.");
  }

  @Override
  public void removeParent(ServerGeoObjectIF parent, ServerHierarchyType hierarchyType)
  {
    String refAttrName = hierarchyType.getParentReferenceAttributeName(parent.getType().getUniversal());

    this.getBusiness().appLock();
    this.getBusiness().setValue(refAttrName, null);
    this.getBusiness().apply();
  }

  @Override
  public ParentTreeNode addParent(ServerGeoObjectIF parent, ServerHierarchyType hierarchyType)
  {
    Universal parentUniversal = parent.getType().getUniversal();
    Universal childUniversal = this.getType().getUniversal();

    String refAttrName = hierarchyType.getParentReferenceAttributeName(parentUniversal);
    String universalRelationshipType = hierarchyType.getUniversalRelationship().definesType();

    GeoEntity.validateUniversalRelationship(childUniversal, parentUniversal, universalRelationshipType);

    this.getBusiness().appLock();
    this.getBusiness().setValue(refAttrName, ( (TreeServerGeoObject) parent ).getGeoEntity().getOid());
    this.getBusiness().apply();

    ParentTreeNode node = new ParentTreeNode(this.getGeoObject(), hierarchyType.getType());
    node.addParent(new ParentTreeNode(parent.getGeoObject(), hierarchyType.getType()));

    return node;
  }

  public void apply(String statusCode, boolean isImport)
  {
    if (!this.getBusiness().isNew())
    {
      this.getBusiness().appLock();
    }

    if (this.getGeoObject().getCode() != null)
    {
      this.getBusiness().setValue(GeoObject.CODE, this.getGeoObject().getCode());
    }

    LocalizedValueConverter.populate((LocalStruct) this.getBusiness().getStruct(GeoObject.DISPLAY_LABEL), this.getGeoObject().getDisplayLabel());

    Geometry geom = this.getGeoObject().getGeometry();
    if (geom != null)
    {
      if (!this.isValidGeometry(geom))
      {
        GeometryTypeException ex = new GeometryTypeException();
        ex.setActualType(geom.getGeometryType());
        ex.setExpectedType(this.getType().getGeometryType().name());

        throw ex;
      }
      else
      {
        this.getBusiness().setValue(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME, geom);
      }
    }

    if (!isImport && !this.getBusiness().isNew() && !this.getGeoObject().getType().isGeometryEditable() && this.getBusiness().isModified(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME))
    {
      throw new GeometryUpdateException();
    }

    Term status = this.populateBusiness(statusCode);
    this.getBusiness().apply();

    /*
     * Update the returned GeoObject
     */
    this.getGeoObject().setStatus(status);
  }

  @Override
  public String bbox()
  {
    String definesType = this.getType().definesType();

    try
    {
      MdBusinessDAOIF mdBusiness = MdBusinessDAO.getMdBusinessDAO(definesType);
      MdAttributeConcreteDAOIF mdAttribute = mdBusiness.definesAttribute(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME);

      StringBuffer sql = new StringBuffer();
      sql.append("SELECT ST_AsText(ST_Extent(" + mdAttribute.getColumnName() + ")) AS bbox");
      sql.append(" FROM " + mdBusiness.getTableName());
      sql.append(" WHERE " + GeoEntity.OID + " = '" + this.getBusiness().getOid() + "'");

      try (ResultSet resultSet = Database.query(sql.toString()))
      {
        if (resultSet.next())
        {
          String bbox = resultSet.getString("bbox");

          if (bbox != null)
          {
            if (bbox.contains("POLYGON"))
            {
              Pattern p = Pattern.compile("POLYGON\\(\\((.*)\\)\\)");
              Matcher m = p.matcher(bbox);

              if (m.matches())
              {
                String coordinates = m.group(1);
                List<Coordinate> coords = new LinkedList<Coordinate>();

                for (String c : coordinates.split(","))
                {
                  String[] xAndY = c.split(" ");
                  double x = Double.valueOf(xAndY[0]);
                  double y = Double.valueOf(xAndY[1]);

                  coords.add(new Coordinate(x, y));
                }

                Envelope e = new Envelope(coords.get(0), coords.get(2));

                JSONArray bboxArr = new JSONArray();
                bboxArr.put(e.getMinX());
                bboxArr.put(e.getMinY());
                bboxArr.put(e.getMaxX());
                bboxArr.put(e.getMaxY());

                return bboxArr.toString();
              }
              else
              {
                throw new ProgrammingErrorException("Pattern did not match on bbox " + bbox);
              }
            }
            else if (bbox.contains("POINT"))
            {
              Pattern p = Pattern.compile("POINT\\((.*)\\)");
              Matcher m = p.matcher(bbox);

              if (m.matches())
              {
                String sCoordinate = m.group(1);

                String[] xAndY = sCoordinate.split(" ");
                double x = Double.valueOf(xAndY[0]);
                double y = Double.valueOf(xAndY[1]);
                Coordinate coordinate = new Coordinate(x, y);

                Envelope e = new Envelope(coordinate, coordinate);

                JSONArray bboxArr = new JSONArray();
                bboxArr.put(e.getMinX());
                bboxArr.put(e.getMinY());
                bboxArr.put(e.getMaxX());
                bboxArr.put(e.getMaxY());

                return bboxArr.toString();
              }
              else
              {
                throw new ProgrammingErrorException("Pattern did not match on bbox " + bbox);
              }
            }
            else
            {
              throw new UnsupportedOperationException("Unsupported bbox geometry type for bbox [" + bbox + "].");
            }
          }
        }
      }
    }
    catch (Exception e)
    {
      logger.error("Unable to compute bounding box for leaf type [" + definesType + "].", e);
    }

    // Extent of the continental United States
    JSONArray bboxArr = new JSONArray();
    bboxArr.put(-125.0011);
    bboxArr.put(24.9493);
    bboxArr.put(-66.9326);
    bboxArr.put(49.5904);

    return bboxArr.toString();

  }
}
