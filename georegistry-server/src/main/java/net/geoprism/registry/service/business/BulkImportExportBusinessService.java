/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.service.business;

import java.io.File;
import java.io.InputStream;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import net.geoprism.registry.ListType;
import net.geoprism.registry.ListTypeQuery;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.permission.RolePermissionService;
import net.geoprism.registry.view.BulkImportSummary;

@Service
public class BulkImportExportBusinessService implements BulkImportExportBusinessServiceIF
{
  @Autowired
  private RolePermissionService                     permissions;

  @Autowired
  private BulkExportServiceIF                       exportService;

  @Autowired
  private BulkImportServiceIF                       importService;

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

  @Autowired
  private ConceptSetBusinessServiceIF               cSetService;

  @Autowired
  private ConceptEdgeTypeBusinessServiceIF          cEdgeTypeService;

  @Autowired
  private ConceptClassBusinessServiceIF             cClassService;

  @Override
  public void createExport(File zipfile, Set<BulkExportSection> sections)
  {
    if (!permissions.isSRA())
    {
      throw new UnsupportedOperationException("Only an SRA can perform a bulk import or export");
    }

    this.exportService.createExport(zipfile, sections);
  }

  @Override
  public void deleteData()
  {
    if (!permissions.isSRA())
    {
      throw new UnsupportedOperationException("Only an SRA can perform a bulk import or export");
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

    this.ugService.getAll().forEach(type -> {
      this.ugService.delete(type);
    });

    this.dagService.getAll().forEach(type -> {
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

    // Delete the classification metadata last: a concept set can't be deleted
    // while a classification attribute of a geo object or business type uses
    // it. Deleting a concept class deletes its terms; its edge types are
    // deleted first because their edges reference the concept classes.
    this.cSetService.getAll().forEach(set -> {
      this.cSetService.delete(set);
    });

    this.cEdgeTypeService.getAll().forEach(type -> {
      this.cEdgeTypeService.delete(type);
    });

    this.cClassService.getAll().forEach(type -> {
      this.cClassService.delete(type);
    });
  }

  @Override
  public BulkImportSummary importData(InputStream stream)
  {
    if (!permissions.isSRA())
    {
      throw new UnsupportedOperationException("Only an SRA can perform a bulk import or export");
    }

    return this.importService.importData(stream);
  }

}
