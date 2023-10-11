package net.geoprism.registry.service;

import java.io.InputStream;

import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import net.geoprism.graphrepo.permission.GeoObjectTypePermissionServiceIF;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.model.ServerOrganization;
import net.geoprism.registry.permission.RolePermissionService;
import net.geoprism.registry.ws.GlobalNotificationMessage;
import net.geoprism.registry.ws.MessageType;
import net.geoprism.registry.ws.NotificationFacade;
import net.geoprism.registry.xml.XMLExporter;

@Repository
public class GPRGeoObjectTypeService extends GeoObjectTypeService implements GeoObjectTypeServiceIF
{
  @Autowired
  private GeoObjectTypePermissionServiceIF typePermissions;

  @Autowired
  private RolePermissionService            rolePermissions;

  @Autowired
  private GraphRepoServiceIF               service;

  @Request(RequestType.SESSION)
  public void importTypes(String sessionId, String orgCode, InputStream istream)
  {
    this.typePermissions.enforceCanCreate(orgCode, true);

    GeoRegistryUtil.importTypes(orgCode, istream);

    this.service.refreshMetadataCache();

    SerializedListTypeCache.getInstance().clear();

    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.TYPE_CACHE_CHANGE, null));
  }

  @Request(RequestType.SESSION)
  public InputStream exportTypes(String sessionId, String code)
  {
    this.rolePermissions.enforceRA(code);

    ServerOrganization organization = ServerOrganization.getByCode(code);

    XMLExporter exporter = new XMLExporter(organization);
    exporter.build();

    return exporter.write();
  }

  @Override
  @Request(RequestType.SESSION)
  public GeoObjectType updateGeoObjectType(String sessionId, String gtJSON)
  {
    GeoObjectType got = super.updateGeoObjectType(sessionId, gtJSON);

    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.TYPE_CACHE_CHANGE, null));

    return got;
  }

  @Override
  @Request(RequestType.SESSION)
  public GeoObjectType createGeoObjectType(String sessionId, String gtJSON)
  {
    GeoObjectType got = super.createGeoObjectType(sessionId, gtJSON);

    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.TYPE_CACHE_CHANGE, null));

    return got;
  }
}
