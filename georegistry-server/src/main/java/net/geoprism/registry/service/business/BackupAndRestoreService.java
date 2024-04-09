package net.geoprism.registry.service.business;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.geoprism.registry.DirectedAcyclicGraphType;
import net.geoprism.registry.UndirectedGraphType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.permission.RolePermissionService;
import net.geoprism.registry.service.request.ServiceFactory;

@Service
public class BackupAndRestoreService implements BackupAndRestoreServiceIF
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

    ServiceFactory.getMetadataCache().getAllGeoObjectTypeCodes().forEach(code -> {
      this.gTypeService.deleteGeoObjectType(code);
    });
  }

  @Override
  public void restoreFromBackup(File zipfile)
  {
    if (!permissions.isSRA())
    {
      throw new UnsupportedOperationException("Only an SRA can create a data dump");
    }

    this.restoreService.restoreFromBackup(zipfile);
  }

}
