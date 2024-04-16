package net.geoprism.registry.service.business;

import java.io.File;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import net.geoprism.registry.DirectedAcyclicGraphType;
import net.geoprism.registry.ListType;
import net.geoprism.registry.ListTypeQuery;
import net.geoprism.registry.UndirectedGraphType;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.permission.RolePermissionService;
import net.geoprism.registry.service.request.GraphRepoServiceIF;
import net.geoprism.registry.service.request.ServiceFactory;

@Service
public class BackupAndRestoreBusinessService implements BackupAndRestoreBusinessServiceIF
{
  @Autowired
  private RolePermissionService                     permissions;

  @Autowired
  private BackupServiceIF                           backupService;

  @Autowired
  private RestoreServiceIF                          restoreService;

  @Autowired
  private BusinessTypeBusinessServiceIF             bTypeService;

  @Autowired
  private BusinessEdgeTypeBusinessServiceIF         bEdgeTypeService;

  @Autowired
  private UndirectedGraphTypeBusinessServiceIF      ugService;

  @Autowired
  private DirectedAcyclicGraphTypeBusinessServiceIF dagService;

  @Autowired
  private GPRTransitionEventBusinessService         teService;

  @Autowired
  private GeoObjectTypeBusinessServiceIF            gTypeService;

  @Autowired
  private HierarchyTypeBusinessServiceIF            hTypeService;

  @Autowired
  private GraphRepoServiceIF                        repoService;

  @Override
  public void createBackup(File zipfile)
  {
    if (!permissions.isSRA())
    {
      throw new UnsupportedOperationException("Only an SRA can create a data dump");
    }

    this.backupService.createBackup(zipfile);
  }

  @Override
  public void deleteData()
  {
    if (!permissions.isSRA())
    {
      throw new UnsupportedOperationException("Only an SRA can create a data dump");
    }

    // Delete all list types
    try (OIterator<? extends ListType> it = new ListTypeQuery(new QueryFactory()).getIterator())
    {
      it.getAll().forEach(list -> list.delete(false));
    }

    this.teService.deleteAll();

    this.bEdgeTypeService.getAll().forEach(type -> {
      this.bEdgeTypeService.delete(type);
    });

    this.bTypeService.getAll().forEach(type -> {
      this.bTypeService.delete(type);
    });

    UndirectedGraphType.getAll().forEach(type -> {
      this.ugService.delete(type);
    });

    DirectedAcyclicGraphType.getAll().forEach(type -> {
      this.dagService.delete(type);
    });

    ServerHierarchyType.getAll().forEach(type -> {
      this.hTypeService.delete(type);
    });

    this.repoService.refreshMetadataCache();

    ServiceFactory.getMetadataCache().getAllGeoObjectTypeCodes().forEach(code -> {
      ServerGeoObjectType type = ServerGeoObjectType.get(code, true);

      if (type != null)
      {
        this.gTypeService.deleteGeoObjectType(code);
      }
    });
  }

  @Override
  public void restoreFromBackup(InputStream stream)
  {
    if (!permissions.isSRA())
    {
      throw new UnsupportedOperationException("Only an SRA can create a data dump");
    }

    this.restoreService.restoreFromBackup(stream);
  }

}
