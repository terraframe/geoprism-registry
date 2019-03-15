package net.geoprism.registry;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.action.AbstractActionDTO;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.runwaysdk.business.rbac.Authenticate;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;

import net.geoprism.registry.action.AbstractAction;
import net.geoprism.registry.action.AllGovernanceStatus;
import net.geoprism.registry.action.ChangeRequest;
import net.geoprism.registry.excel.GeoObjectExcelExporter;
import net.geoprism.registry.excel.MasterListExcelExporter;
import net.geoprism.registry.query.GeoObjectQuery;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.shapefile.GeoObjectShapefileExporter;
import net.geoprism.registry.shapefile.MasterListShapefileExporter;

public class GeoRegistryUtil extends GeoRegistryUtilBase
{
  private static final long serialVersionUID = 2034796376;

  public GeoRegistryUtil()
  {
    super();
  }

  @Authenticate
  public static String createHierarchyType(String htJSON)
  {
    RegistryAdapter adapter = ServiceFactory.getAdapter();

    HierarchyType hierarchyType = HierarchyType.fromJSON(htJSON, adapter);

    hierarchyType = ServiceFactory.getConversionService().createHierarchyType(hierarchyType);

    // The transaction did not error out, so it is safe to put into the cache.
    ServiceFactory.getAdapter().getMetadataCache().addHierarchyType(hierarchyType);

    return hierarchyType.getCode();
  }

  @Transaction
  public static void submitChangeRequest(String sJson)
  {
    ChangeRequest cr = new ChangeRequest();
    cr.addApprovalStatus(AllGovernanceStatus.PENDING);
    cr.apply();

    List<AbstractActionDTO> actionDTOs = AbstractActionDTO.parseActions(sJson);

    for (AbstractActionDTO actionDTO : actionDTOs)
    {
      AbstractAction ra = AbstractAction.dtoToRegistry(actionDTO);
      ra.addApprovalStatus(AllGovernanceStatus.PENDING);
      ra.apply();

      cr.addAction(ra).apply();
    }
  }

  @Authenticate
  @Transaction
  public static InputStream exportShapefile(String code, String hierarchyCode)
  {
    HierarchyType hierarchyType = ServiceFactory.getAdapter().getMetadataCache().getHierachyType(hierarchyCode).get();
    GeoObjectQuery query = ServiceFactory.getRegistryService().createQuery(code);
    OIterator<GeoObject> it = null;

    try
    {
      it = query.getIterator();

      GeoObjectShapefileExporter exporter = new GeoObjectShapefileExporter(query.getType(), hierarchyType, it);

      return exporter.export();
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
    finally
    {
      if (it != null)
      {
        it.close();
      }
    }
  }

  @Authenticate
  @Transaction
  public static InputStream exportSpreadsheet(String code, String hierarchyCode)
  {
    HierarchyType hierarchyType = ServiceFactory.getAdapter().getMetadataCache().getHierachyType(hierarchyCode).get();
    GeoObjectQuery query = ServiceFactory.getRegistryService().createQuery(code);
    OIterator<GeoObject> it = null;

    try
    {
      it = query.getIterator();

      GeoObjectExcelExporter exporter = new GeoObjectExcelExporter(query.getType(), hierarchyType, it);
      InputStream istream = exporter.export();

      return istream;
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
    finally
    {
      if (it != null)
      {
        it.close();
      }
    }
  }

  @Transaction
  public static InputStream exportMasterListShapefile(String oid)
  {
    MasterList list = MasterList.get(oid);
    MdBusinessDAOIF mdBusiness = MdBusinessDAO.get(list.getMdBusinessOid());

    List<? extends MdAttributeConcreteDAOIF> mdAttributes = mdBusiness.definesAttributesOrdered().stream().filter(mdAttribute -> list.isValid(mdAttribute)).collect(Collectors.toList());

    try
    {
      MasterListShapefileExporter exporter = new MasterListShapefileExporter(list, mdBusiness, mdAttributes);

      return exporter.export();
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  @Transaction
  public static InputStream exportMasterListExcel(String oid)
  {
    MasterList list = MasterList.get(oid);
    MdBusinessDAOIF mdBusiness = MdBusinessDAO.get(list.getMdBusinessOid());
    
    List<? extends MdAttributeConcreteDAOIF> mdAttributes = mdBusiness.definesAttributesOrdered().stream().filter(mdAttribute -> list.isValid(mdAttribute)).collect(Collectors.toList());
    
    try
    {
      MasterListExcelExporter exporter = new MasterListExcelExporter(list, mdBusiness, mdAttributes);
      
      return exporter.export();
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }
  
}
