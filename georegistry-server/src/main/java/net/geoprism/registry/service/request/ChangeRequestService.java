/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.service.request;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.commongeoregistry.adapter.metadata.RegistryRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.business.rbac.RoleDAOIF;
import com.runwaysdk.business.rbac.SingleActorDAOIF;
import com.runwaysdk.dataaccess.ValueObject;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.AttributeLocal;
import com.runwaysdk.query.Condition;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.OrderBy.SortOrder;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.query.Selectable;
import com.runwaysdk.query.ValueQuery;
import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.VaultFile;

import net.geoprism.registry.CGRPermissionException;
import net.geoprism.registry.Organization;
import net.geoprism.registry.action.AbstractAction;
import net.geoprism.registry.action.AllGovernanceStatus;
import net.geoprism.registry.action.ChangeRequest;
import net.geoprism.registry.action.ChangeRequest.ChangeRequestType;
import net.geoprism.registry.action.ChangeRequestQuery;
import net.geoprism.registry.action.InvalidChangeRequestException;
import net.geoprism.registry.action.geoobject.CreateGeoObjectAction;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.service.business.GeoObjectEditorBusinessService;
import net.geoprism.registry.service.business.GeoObjectTypeBusinessServiceIF;
import net.geoprism.registry.service.permission.ChangeRequestPermissionService;
import net.geoprism.registry.service.permission.ChangeRequestPermissionService.ChangeRequestPermissionAction;
import net.geoprism.registry.view.Page;

@Service
public class ChangeRequestService
{
  @Autowired
  public ChangeRequestPermissionService permissions;

  @Autowired
  public GeoObjectTypeBusinessServiceIF typeService;

  @Autowired
  public GeoObjectEditorBusinessService editorService;

  @Request(RequestType.SESSION)
  public void reject(String sessionId, String request)
  {
    ChangeRequest input = ChangeRequest.fromJSON(request);
    ChangeRequest current = ChangeRequest.get(JsonParser.parseString(request).getAsJsonObject().get("oid").getAsString());

    if (!this.permissions.getPermissions(current).containsAll(Arrays.asList(ChangeRequestPermissionAction.WRITE, ChangeRequestPermissionAction.WRITE_APPROVAL_STATUS, ChangeRequestPermissionAction.READ, ChangeRequestPermissionAction.READ_DETAILS)))
    {
      throw new CGRPermissionException();
    }

    current.reject(input.getMaintainerNotes(), input.getAdditionalNotes());
  }

  @Request(RequestType.SESSION)
  public void deleteDocumentCR(String sessionId, String crOid, String vfOid)
  {
    this.deleteDocumentInTransCR(crOid, vfOid);
  }

  @Transaction
  void deleteDocumentInTransCR(String crOid, String vfOid)
  {
    ChangeRequest request = ChangeRequest.get(crOid);

    if (!this.permissions.getPermissions(request).contains(ChangeRequestPermissionAction.WRITE_DOCUMENTS))
    {
      throw new CGRPermissionException();
    }

    VaultFile vf = VaultFile.get(vfOid);

    vf.delete();
  }

  @Request(RequestType.SESSION)
  public ApplicationResource downloadDocumentCR(String sessionId, String crOid, String vfOid)
  {
    return this.downloadDocumentCR(crOid, vfOid);
  }

  ApplicationResource downloadDocumentCR(String crOid, String vfOid)
  {
    ChangeRequest request = ChangeRequest.get(crOid);

    if (!this.permissions.getPermissions(request).contains(ChangeRequestPermissionAction.READ_DOCUMENTS))
    {
      throw new CGRPermissionException();
    }

    VaultFile vf = VaultFile.get(vfOid);

    return vf;
  }

  @Request(RequestType.SESSION)
  public String listDocumentsCR(String sessionId, String requestId)
  {
    return this.listDocumentsCR(requestId);
  }

  String listDocumentsCR(String requestId)
  {
    JsonArray ja = new JsonArray();

    ChangeRequest request = ChangeRequest.get(requestId);

    if (!this.permissions.getPermissions(request).contains(ChangeRequestPermissionAction.READ_DOCUMENTS))
    {
      throw new CGRPermissionException();
    }

    OIterator<? extends VaultFile> it = request.getAllDocument();
    try
    {
      for (VaultFile vf : it)
      {
        JsonObject jo = new JsonObject();

        jo.addProperty("fileName", vf.getName());
        jo.addProperty("oid", vf.getOid());
        jo.addProperty("requestId", requestId);

        ja.add(jo);
      }
    }
    finally
    {
      it.close();
    }

    return ja.toString();
  }

  @Request(RequestType.SESSION)
  public String uploadFileCR(String sessionId, String requestId, String fileName, InputStream fileStream)
  {
    return uploadFileInTransactionCR(requestId, fileName, fileStream);
  }

  @Transaction
  public String uploadFileInTransactionCR(String requestId, String fileName, InputStream fileStream)
  {
    ChangeRequest request = ChangeRequest.get(requestId);

    if (!this.permissions.getPermissions(request).contains(ChangeRequestPermissionAction.WRITE_DOCUMENTS))
    {
      throw new CGRPermissionException();
    }

    VaultFile vf = VaultFile.createAndApply(fileName, fileStream);

    request.addDocument(vf).apply();

    JsonObject jo = new JsonObject();

    jo.addProperty("fileName", vf.getName());
    jo.addProperty("oid", vf.getOid());
    jo.addProperty("requestId", requestId);

    return jo.toString();
  }

  @Request(RequestType.SESSION)
  public JsonObject getAllRequestsSerialized(String sessionId, int pageSize, int pageNumber, String filter, String sort, String oid)
  {
    return this.getAllRequests(sessionId, pageSize, pageNumber, filter, sort, oid).toJSON();
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Request(RequestType.SESSION)
  public Page<ChangeRequest> getAllRequests(String sessionId, int pageSize, int pageNumber, String filter, String sort, String oid)
  {
    ChangeRequestQuery query = new ChangeRequestQuery(new QueryFactory());

    if (filter != null && filter.length() > 0 && !filter.equals("ALL"))
    {
      query.WHERE(query.getApprovalStatus().containsAll(AllGovernanceStatus.valueOf(filter)));
    }

    filterQueryBasedOnPermissions(query);

    if (oid != null && oid.length() > 0)
    {
      pageNumber = this.findPageNumber(oid, query, pageSize);
    }

    query.restrictRows(pageSize, pageNumber);

    if (sort != null && sort.length() > 0 && !sort.equals("[]"))
    {
      JsonArray ja = JsonParser.parseString(sort).getAsJsonArray();

      for (int i = 0; i < ja.size(); ++i)
      {
        JsonObject jo = ja.get(i).getAsJsonObject();

        boolean ascending = jo.get("ascending").getAsBoolean();
        String attribute = jo.get("attribute").getAsString();

        Selectable sel = query.get(attribute);

        if (attribute.equals(ChangeRequest.GEOOBJECTLABEL) || attribute.equals(ChangeRequest.GEOOBJECTTYPELABEL))
        {
          sel = ( (AttributeLocal) sel ).localize();
        }
        else if (attribute.equals(ChangeRequest.APPROVALSTATUS))
        {
          sel = query.getApprovalStatus().getEnumName();
        }

        query.ORDER_BY(sel, ascending ? SortOrder.ASC : SortOrder.DESC);
      }
    }
    else
    {
      query.ORDER_BY_DESC(query.getCreateDate());
    }

    List<? extends ChangeRequest> list = query.getIterator().getAll();

    for (ChangeRequest cr : list)
    {
      if (ServerGeoObjectType.get(cr.getGeoObjectTypeCode(), true) != null)
      {
        cr.lock();
        cr.clearApprovalStatus();
        cr.addApprovalStatus(AllGovernanceStatus.INVALID);
        cr.apply();
      }
    }

    return new Page(query.getCount(), pageNumber, pageSize, list);
  }

  // An attempt to do this without selectable sqls
  // private int findPageNumber(String crOid, ChangeRequestQuery query, int
  // pageSize)
  // {
  // QueryFactory qf = new QueryFactory();
  // ValueQuery vq = new ValueQuery(qf);
  //
  // vq.FROM(query);
  //
  // vq.SELECT(query.getOid());
  // vq.SELECT(query.getCreateDate());
  //
  // Selectable createDate = query.getCreateDate();
  //
  // vq.SELECT(vq.RANK("rn").OVER(null, new OrderBy(createDate,
  // SortOrder.DESC)));
  //
  // vq.WHERE(query.getOid().EQ(crOid));
  //
  // ValueObject vo = vq.getIterator().getAll().get(0);
  //
  // long rowNum = Long.parseLong(vo.getValue("rn"));
  //
  // int pageNum = (int) ( (rowNum / pageSize) + 1 );
  //
  // return pageNum;
  // }

  private int findPageNumber(String crOid, ChangeRequestQuery query, int pageSize)
  {
    QueryFactory qf = new QueryFactory();
    ValueQuery innerVq = new ValueQuery(qf);

    String sub1Sql = query.getSQL();

    innerVq.FROM("(" + sub1Sql + ")", "sub1");

    String createDateAlias = query.getCreateDate().getColumnAlias();

    Matcher m = Pattern.compile("change_request_\\d+\\.create_date AS (create_date_\\d+),").matcher(sub1Sql);
    if (m.find())
    {
      createDateAlias = m.group(1);
    }

    innerVq.SELECT(innerVq.aSQLCharacter("oid", "oid"));

    // The rank function is forcing a group by, which we don't want to do. It
    // also doesn't use our alias.
    // SelectableSQLDate createDate =
    // innerVq.aSQLDate(query.getCreateDate().getColumnAlias(),
    // query.getCreateDate().getColumnAlias());
    // AggregateFunction rank = innerVq.RANK("rn").OVER(null, new
    // OrderBy(createDate, SortOrder.DESC));
    Selectable rank = innerVq.aSQLInteger("rn", "(ROW_NUMBER() OVER (ORDER BY " + createDateAlias + " DESC))");

    innerVq.SELECT(rank);

    ValueQuery outerVq = new ValueQuery(qf);

    outerVq.FROM("(" + innerVq.getSQL() + ")", "sub2");

    Selectable oidSel = outerVq.aSQLCharacter("oid", "oid");

    outerVq.SELECT(oidSel);
    outerVq.SELECT(outerVq.aSQLInteger("rn", "rn"));

    outerVq.WHERE(oidSel.EQ(crOid));

    List<ValueObject> voList = outerVq.getIterator().getAll();

    if (voList.size() > 0)
    {
      ValueObject vo = voList.get(0);

      long rowNum = Long.parseLong(vo.getValue("rn"));

      int pageNum = (int) ( ( rowNum / pageSize ) + 1 );

      return pageNum;
    }
    else
    {
      return 1;
    }
  }

  public void filterQueryBasedOnPermissions(ChangeRequestQuery crq)
  {
    List<String> raOrgs = new ArrayList<String>();
    List<String> goRoles = new ArrayList<String>();

    Condition cond = null;

    SingleActorDAOIF actor = Session.getCurrentSession().getUser();
    for (RoleDAOIF role : actor.authorizedRoles())
    {
      String roleName = role.getRoleName();

      if (RegistryRole.Type.isOrgRole(roleName) && !RegistryRole.Type.isRootOrgRole(roleName))
      {
        if (RegistryRole.Type.isRA_Role(roleName))
        {
          String roleOrgCode = RegistryRole.Type.parseOrgCode(roleName);
          raOrgs.add(roleOrgCode);
        }
        else if (RegistryRole.Type.isRM_Role(roleName) || RegistryRole.Type.isRC_Role(roleName) || RegistryRole.Type.isAC_Role(roleName))
        {
          goRoles.add(roleName);
        }
      }
    }

    for (String orgCode : raOrgs)
    {
      Organization org = Organization.getByCode(orgCode);

      Condition loopCond = crq.getOrganizationCode().EQ(org.getCode());

      if (cond == null)
      {
        cond = loopCond;
      }
      else
      {
        cond = cond.OR(loopCond);
      }
    }

    for (String roleName : goRoles)
    {
      String roleOrgCode = RegistryRole.Type.parseOrgCode(roleName);
      Organization org = Organization.getByCode(roleOrgCode);
      String gotCode = RegistryRole.Type.parseGotCode(roleName);

      Condition loopCond = crq.getGeoObjectTypeCode().EQ(gotCode).AND(crq.getOrganizationCode().EQ(org.getCode()));

      if (cond == null)
      {
        cond = loopCond;
      }
      else
      {
        cond = cond.OR(loopCond);
      }

      // If they have permission to an abstract parent type, then they also have
      // permission to all its children.
      ServerGeoObjectType type = ServerGeoObjectType.get(gotCode, true);

      if (type != null && type.getIsAbstract())
      {
        List<ServerGeoObjectType> subTypes = this.typeService.getSubtypes(type);

        for (ServerGeoObjectType subType : subTypes)
        {
          Condition superCond = crq.getGeoObjectTypeCode().EQ(subType.getCode()).AND(crq.getOrganizationCode().EQ(subType.getOrganization().getCode()));

          cond = cond.OR(superCond);
        }
      }
    }

    if (cond != null)
    {
      crq.AND(cond);
    }
  }

  @Request(RequestType.SESSION)
  public void setActionStatus(String sessionId, String actionOid, String status)
  {
    AbstractAction action = AbstractAction.get(actionOid);

    if (!this.permissions.getPermissions(action.getAllRequest().next()).containsAll(Arrays.asList(ChangeRequestPermissionAction.WRITE_APPROVAL_STATUS)))
    {
      throw new CGRPermissionException();
    }

    action.appLock();
    action.clearApprovalStatus();
    action.addApprovalStatus(AllGovernanceStatus.valueOf(status));
    action.apply();
  }

  @Request(RequestType.SESSION)
  public JsonObject implementDecisions(String sessionId, String request, String newCode)
  {
    ChangeRequest input = ChangeRequest.fromJSON(request);
    ChangeRequest current = ChangeRequest.get(JsonParser.parseString(request).getAsJsonObject().get("oid").getAsString());

    Set<ChangeRequestPermissionAction> permissions = this.permissions.getPermissions(current);

    // Allow them to also update the code when they execute the CR (for creating
    // new GeoObjects)
    if (ChangeRequestType.CreateGeoObject.equals(current.getChangeRequestType()) && StringUtils.isNotEmpty(newCode))
    {
      java.util.Optional<? extends AbstractAction> opAction = current.getAllAction().getAll().stream().findFirst();

      if (!opAction.isPresent() || ! ( opAction.get() instanceof CreateGeoObjectAction ))
      {
        throw new InvalidChangeRequestException();
      }
      else if (!permissions.contains(ChangeRequestPermissionAction.WRITE_CODE))
      {
        throw new CGRPermissionException();
      }
      else
      {
        CreateGeoObjectAction createAction = (CreateGeoObjectAction) opAction.get();
        createAction.appLock();

        JsonObject joGO = JsonParser.parseString(createAction.getGeoObjectJson()).getAsJsonObject();
        joGO.get("attributes").getAsJsonObject().addProperty("code", newCode);
        createAction.setGeoObjectJson(joGO.toString());

        createAction.apply();

        current.appLock();
        current.setGeoObjectCode(newCode);
        current.apply();
      }
    }

    if (!permissions.containsAll(Arrays.asList(ChangeRequestPermissionAction.EXECUTE, ChangeRequestPermissionAction.WRITE_APPROVAL_STATUS, ChangeRequestPermissionAction.WRITE)))
    {
      throw new CGRPermissionException();
    }

    current.execute(input.getMaintainerNotes(), input.getAdditionalNotes());

    return current.getDetails();
  }

  @Request(RequestType.SESSION)
  public JsonObject update(String sessionId, JsonObject request)
  {
    String oid = request.get("oid").getAsString();
    JsonArray actions = request.get("actions").getAsJsonArray();
    String notes = request.get("contributorNotes").getAsString();

    ChangeRequest current = ChangeRequest.get(oid);

    this.editorService.updateChangeRequest(current, notes, actions);

    return current.getDetails();
  }

  @Request(RequestType.SESSION)
  public String deleteChangeRequest(String sessionId, String requestId)
  {
    ChangeRequest request = ChangeRequest.get(requestId);

    if (!this.permissions.getPermissions(request).containsAll(Arrays.asList(ChangeRequestPermissionAction.DELETE)))
    {
      throw new CGRPermissionException();
    }

    request.delete();

    return requestId;
  }
}
