package net.geoprism.registry.service.business;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.commongeoregistry.adapter.metadata.RegistryRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.business.rbac.RoleDAOIF;
import com.runwaysdk.business.rbac.SingleActorDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.Condition;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.OrderBy.SortOrder;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.VaultFile;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.ExecutableJob;
import com.runwaysdk.system.scheduler.JobHistory;
import com.runwaysdk.system.scheduler.JobHistoryRecord;

import net.geoprism.GeoprismUser;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.Organization;
import net.geoprism.registry.etl.DataImportJob;
import net.geoprism.registry.etl.ImportError;
import net.geoprism.registry.etl.ImportError.ErrorResolution;
import net.geoprism.registry.etl.ImportErrorQuery;
import net.geoprism.registry.etl.ImportHistory;
import net.geoprism.registry.etl.ImportHistoryQuery;
import net.geoprism.registry.etl.ImportStage;
import net.geoprism.registry.etl.ParentReferenceProblem;
import net.geoprism.registry.etl.TermReferenceProblem;
import net.geoprism.registry.etl.ValidationProblem;
import net.geoprism.registry.etl.ValidationProblem.ValidationResolution;
import net.geoprism.registry.etl.ValidationProblemQuery;
import net.geoprism.registry.etl.export.DataExportJob;
import net.geoprism.registry.etl.export.ExportError;
import net.geoprism.registry.etl.export.ExportErrorQuery;
import net.geoprism.registry.etl.export.ExportHistory;
import net.geoprism.registry.etl.upload.ImportConfiguration;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.service.permission.RolePermissionService;
import net.geoprism.registry.view.JsonWrapper;
import net.geoprism.registry.view.Page;
import net.geoprism.registry.view.ServerParentTreeNodeOverTime;

@Service
public class ETLBusinessService
{
  @Autowired
  protected ClassifierBusinessServiceIF    classifierService;

  @Autowired
  protected GeoObjectTypeBusinessServiceIF typeService;

  @Autowired
  protected GeoObjectBusinessServiceIF     objectService;

  @Autowired
  protected TermBusinessServiceIF          termService;

  @Autowired
  protected GeoSynonymBusinessService      geoSynonymService;

  @Autowired
  protected GeoObjectEditorBusinessService editorService;

  public JsonObject doImport(String json)
  {
    ImportConfiguration config = ImportConfiguration.build(json);
    config.enforceExecutePermissions();

    ImportHistory hist;

    if (config.getHistoryId() != null && config.getHistoryId().length() > 0)
    {
      String historyId = config.getHistoryId();
      hist = ImportHistory.get(historyId);

      JobHistoryRecord record = hist.getAllJobRel().getAll().get(0);
      ExecutableJob execJob = record.getParent();

      execJob.resume(record);
    }
    else
    {
      DataImportJob job = new DataImportJob();
      job.setRunAsUserId(Session.getCurrentSession().getUser().getOid());
      job.apply();

      hist = job.start(config);
    }

    return JsonParser.parseString(hist.getConfigJson()).getAsJsonObject();
  }

  @Transaction
  public void reImport(MultipartFile file, String json)
  {
    ImportConfiguration config = ImportConfiguration.build(json);

    ImportHistory hist = ImportHistory.get(config.getHistoryId());
    hist.getConfig().enforceExecutePermissions();

    VaultFile vf = VaultFile.get(config.getVaultFileId());
    vf.delete();

    VaultFile vf2 = null;
    try (InputStream is = file.getInputStream())
    {
      vf2 = VaultFile.createAndApply(file.getOriginalFilename(), is);
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }

    config.setVaultFileId(vf2.getOid());
    config.setFileName(file.getOriginalFilename());

    hist = ImportHistory.lock(config.getHistoryId());
    hist.setImportFile(vf2);
    hist.setConfigJson(config.toJSON().toString());
    hist.apply();
  }

  public JsonObject getImportErrors(String historyId, boolean onlyUnresolved, int pageSize, int pageNumber)
  {
    ImportErrorQuery query = new ImportErrorQuery(new QueryFactory());

    query.WHERE(query.getHistory().EQ(historyId));

    if (onlyUnresolved)
    {
      query.WHERE(query.getResolution().EQ(ErrorResolution.UNRESOLVED.name()));
    }

    query.ORDER_BY(query.getRowIndex(), SortOrder.ASC);

    query.restrictRows(pageSize, pageNumber);

    try (OIterator<? extends ImportError> it = query.getIterator())
    {
      List<ImportError> results = new LinkedList<>(it.getAll());

      return new Page<ImportError>(query.getCount(), query.getPageNumber(), query.getPageSize(), results).toJSON();
    }
  }

  public void filterHistoryQueryBasedOnPermissions(ImportHistoryQuery ihq)
  {
    List<String> raOrgs = new ArrayList<String>();
    List<String> rmGeoObjects = new ArrayList<String>();

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
        else if (RegistryRole.Type.isRM_Role(roleName))
        {
          rmGeoObjects.add(roleName);
        }
      }
    }

    if (!new RolePermissionService().isSRA() && raOrgs.size() == 0 && rmGeoObjects.size() == 0)
    {
      throw new ProgrammingErrorException("This endpoint must be invoked by an RA or RM");
    }

    for (String orgCode : raOrgs)
    {
      Organization org = Organization.getByCode(orgCode);

      Condition loopCond = ihq.getOrganization().EQ(org);

      if (cond == null)
      {
        cond = loopCond;
      }
      else
      {
        cond = cond.OR(loopCond);
      }
    }

    for (String roleName : rmGeoObjects)
    {
      String roleOrgCode = RegistryRole.Type.parseOrgCode(roleName);
      Organization org = Organization.getByCode(roleOrgCode);
      String gotCode = RegistryRole.Type.parseGotCode(roleName);

      Condition loopCond = ihq.getGeoObjectTypeCode().EQ(gotCode).AND(ihq.getOrganization().EQ(org));

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
          Condition superCond = ihq.getGeoObjectTypeCode().EQ(subType.getCode()).AND(ihq.getOrganization().EQ(subType.getOrganization().getOrganization()));

          cond = cond.OR(superCond);
        }
      }
    }

    if (cond != null)
    {
      ihq.AND(cond);
    }
  }

  @Transaction
  public void cancelImport(String json)
  {
    // This code can fail if it references a GeoObjectType which no longer
    // exists
    // ImportConfiguration config = ImportConfiguration.build(json);
    // final String vaultId = config.getVaultFileId();
    // final String historyId = config.getHistoryId();

    JsonObject jo = JsonParser.parseString(json).getAsJsonObject();
    final String vaultId = jo.has(GeoObjectImportConfiguration.VAULT_FILE_ID) ? jo.get(GeoObjectImportConfiguration.VAULT_FILE_ID).getAsString() : null;
    final String historyId = jo.has(GeoObjectImportConfiguration.HISTORY_ID) ? jo.get(GeoObjectImportConfiguration.HISTORY_ID).getAsString() : null;

    if (StringUtils.isNotEmpty(vaultId))
    {
      VaultFile.get(vaultId).delete();
    }

    // This code is also invoked when they hit "cancel" on the import modal, at
    // which point it won't have a historyId.
    if (StringUtils.isNotEmpty(historyId))
    {
      ImportHistory hist = ImportHistory.get(historyId);

      try
      {
        hist.getConfig().enforceExecutePermissions();
      }
      catch (net.geoprism.registry.DataNotFoundException ex)
      {
        // If we can't construct (because the type no longer exists), just
        // ignore it and allow delete since it's corrupt anyway.
      }

      if (!hist.getStage().get(0).equals(ImportStage.VALIDATION_RESOLVE))
      {
        throw new ProgrammingErrorException("Import jobs can only be canceled if they are in " + ImportStage.VALIDATION_RESOLVE.name() + " stage.");
      }

      ValidationProblemQuery vpq = new ValidationProblemQuery(new QueryFactory());
      vpq.WHERE(vpq.getHistory().EQ(historyId));
      OIterator<? extends ValidationProblem> it = vpq.getIterator();
      try
      {
        while (it.hasNext())
        {
          it.next().delete();
        }
      }
      finally
      {
        it.close();
      }

      hist = ImportHistory.lock(historyId);
      hist.clearStage();
      hist.addStage(ImportStage.COMPLETE);
      hist.clearStatus();
      hist.addStatus(AllJobStatus.CANCELED);
      hist.setImportedRecords(0L);
      hist.apply();
    }
  }

  public JsonObject getActiveImports(int pageSize, int pageNumber, String sortAttr, boolean isAscending)
  {
    JsonArray ja = new JsonArray();

    QueryFactory qf = new QueryFactory();
    ImportHistoryQuery ihq = new ImportHistoryQuery(qf);
    ihq.WHERE(ihq.getStatus().containsExactly(AllJobStatus.RUNNING).OR(ihq.getStatus().containsExactly(AllJobStatus.NEW)).OR(ihq.getStatus().containsExactly(AllJobStatus.QUEUED)).OR(ihq.getStatus().containsExactly(AllJobStatus.FEEDBACK)));

    this.filterHistoryQueryBasedOnPermissions(ihq);

    ihq.restrictRows(pageSize, pageNumber);
    ihq.ORDER_BY(ihq.get(sortAttr), isAscending ? SortOrder.ASC : SortOrder.DESC);

    try (OIterator<? extends ImportHistory> it = ihq.getIterator())
    {
      List<JsonWrapper> results = it.getAll().stream().map(hist -> {
        DataImportJob job = (DataImportJob) hist.getAllJob().getAll().get(0);
        GeoprismUser user = ( job.getRunAsUser() == null ) ? null : GeoprismUser.get(job.getRunAsUser().getOid());

        return new JsonWrapper(serializeHistory(hist, user, job));
      }).collect(Collectors.toList());

      return new Page<JsonWrapper>(ihq.getCount(), ihq.getPageNumber(), ihq.getPageSize(), results).toJSON();
    }
  }

  public JsonObject getCompletedImports(int pageSize, int pageNumber, String sortAttr, boolean isAscending)
  {
    QueryFactory qf = new QueryFactory();
    ImportHistoryQuery ihq = new ImportHistoryQuery(qf);
    ihq.WHERE(ihq.getStatus().containsExactly(AllJobStatus.SUCCESS).OR(ihq.getStatus().containsExactly(AllJobStatus.FAILURE)).OR(ihq.getStatus().containsExactly(AllJobStatus.CANCELED)));

    this.filterHistoryQueryBasedOnPermissions(ihq);

    ihq.restrictRows(pageSize, pageNumber);
    ihq.ORDER_BY(ihq.get(sortAttr), isAscending ? SortOrder.ASC : SortOrder.DESC);

    try (OIterator<? extends ImportHistory> it = ihq.getIterator())
    {
      List<JsonWrapper> results = it.getAll().stream().map(hist -> {
        DataImportJob job = (DataImportJob) hist.getAllJob().getAll().get(0);
        GeoprismUser user = ( job.getRunAsUser() == null ) ? null : GeoprismUser.get(job.getRunAsUser().getOid());

        return new JsonWrapper(serializeHistory(hist, user, job));
      }).collect(Collectors.toList());

      return new Page<JsonWrapper>(ihq.getCount(), ihq.getPageNumber(), ihq.getPageSize(), results).toJSON();
    }
  }

  protected JsonObject serializeHistory(JobHistory hist, GeoprismUser user, ExecutableJob job)
  {
    JsonObject jo = new JsonObject();

    jo.addProperty("jobType", job.getType());
    jo.addProperty("status", hist.getStatus().get(0).name());
    jo.addProperty("author", user == null ? "SYSTEM" : user.getUsername());
    jo.addProperty("createDate", GeoRegistryUtil.formatDate(hist.getCreateDate(), false));
    jo.addProperty("lastUpdateDate", GeoRegistryUtil.formatDate(hist.getLastUpdateDate(), false));
    jo.addProperty("workProgress", hist.getWorkProgress());
    jo.addProperty("workTotal", hist.getWorkTotal());
    jo.addProperty("historyId", hist.getOid());
    jo.addProperty("jobId", job.getOid());

    if (hist instanceof ImportHistory)
    {
      ImportHistory iHist = (ImportHistory) hist;

      jo.addProperty("importedRecords", iHist.getImportedRecords());

      jo.addProperty("stage", iHist.getStage().get(0).name());

      JsonObject config = JsonParser.parseString(iHist.getConfigJson()).getAsJsonObject();
      jo.addProperty("fileName", config.get(ImportConfiguration.FILE_NAME).getAsString());
      jo.add("configuration", JsonParser.parseString(config.toString()));
    }
    else if (hist instanceof ExportHistory)
    {
      ExportHistory eHist = (ExportHistory) hist;

      jo.addProperty("exportedRecords", eHist.getExportedRecords());

      jo.addProperty("stage", eHist.getStage().get(0).name());
    }

    if (job instanceof DataExportJob)
    {
      jo.addProperty("configuration", ( (DataExportJob) job ).getConfigOid());
    }

    if (hist.getStatus().get(0).equals(AllJobStatus.FAILURE) && hist.getErrorJson().length() > 0)
    {
      JsonObject exception = new JsonObject();

      exception.add("type", JsonParser.parseString(hist.getErrorJson()).getAsJsonObject().get("type"));
      exception.addProperty("message", hist.getLocalizedError(Session.getCurrentLocale()));

      jo.add("exception", exception);
    }

    return jo;
  }

  public JsonObject getValidationProblems(String historyId, boolean onlyUnresolved, int pageSize, int pageNumber)
  {
    ImportHistory hist = ImportHistory.get(historyId);

    ValidationProblemQuery vpq = new ValidationProblemQuery(new QueryFactory());
    vpq.WHERE(vpq.getHistory().EQ(hist));
    vpq.restrictRows(pageSize, pageNumber);
    vpq.ORDER_BY(vpq.getSeverity(), SortOrder.DESC);
    vpq.ORDER_BY(vpq.getAffectedRows(), SortOrder.ASC);

    if (onlyUnresolved)
    {
      vpq.WHERE(vpq.getResolution().EQ(ErrorResolution.UNRESOLVED.name()));
    }

    try (OIterator<? extends ValidationProblem> it = vpq.getIterator())
    {
      List<ValidationProblem> results = new LinkedList<>(it.getAll());

      return new Page<ValidationProblem>(vpq.getCount(), vpq.getPageNumber(), vpq.getPageSize(), results).toJSON();
    }
  }

  public JsonObject getExportErrors(String historyId, int pageSize, int pageNumber)
  {
    ExportErrorQuery query = new ExportErrorQuery(new QueryFactory());

    query.WHERE(query.getHistory().EQ(historyId));

    query.ORDER_BY(query.getRowIndex(), SortOrder.ASC);

    query.restrictRows(pageSize, pageNumber);

    try (OIterator<? extends ExportError> it = query.getIterator())
    {
      List<ExportError> results = new LinkedList<>(it.getAll());

      return new Page<ExportError>(query.getCount(), query.getPageNumber(), query.getPageSize(), results).toJSON();
    }
  }

  public JsonObject getImportDetails(String historyId, boolean onlyUnresolved, int pageSize, int pageNumber)
  {
    ImportHistory hist = ImportHistory.get(historyId);
    DataImportJob job = (DataImportJob) hist.getAllJob().getAll().get(0);
    GeoprismUser user = ( job.getRunAsUser() == null ) ? null : GeoprismUser.get(job.getRunAsUser().getOid());
    hist.getConfig().enforceExecutePermissions();

    JsonObject jo = this.serializeHistory(hist, user, job);

    if (hist.getStage().get(0).equals(ImportStage.IMPORT_RESOLVE) && hist.hasImportErrors())
    {
      jo.add("importErrors", this.getImportErrors(historyId, onlyUnresolved, pageSize, pageNumber));
    }
    else if (hist.getStage().get(0).equals(ImportStage.VALIDATION_RESOLVE))
    {
      jo.add("problems", this.getValidationProblems(historyId, onlyUnresolved, pageSize, pageNumber));
    }

    return jo;
  }

  public JsonObject getExportDetails(String historyId, int pageSize, int pageNumber)
  {
    ExportHistory hist = ExportHistory.get(historyId);
    DataExportJob job = (DataExportJob) hist.getAllJob().getAll().get(0);
    GeoprismUser user = ( job.getRunAsUser() == null ) ? null : GeoprismUser.get(job.getRunAsUser().getOid());

    JsonObject jo = this.serializeHistory(hist, user, job);

    jo.add("exportErrors", this.getExportErrors(historyId, pageSize, pageNumber));

    return jo;
  }

  @Transaction
  public void submitImportErrorResolution(String json)
  {
    JsonObject config = JsonParser.parseString(json).getAsJsonObject();

    ImportHistory hist = ImportHistory.get(config.get("historyId").getAsString());
    hist.getConfig().enforceExecutePermissions();

    ImportError err = ImportError.get(config.get("importErrorId").getAsString());

    String resolution = config.get("resolution").getAsString();

    if (resolution.equals(ErrorResolution.APPLY_GEO_OBJECT.name()))
    {
      String parentTreeNode = config.get("parentTreeNode").toString();
      String geoObject = config.get("geoObject").toString();
      Boolean isNew = config.get("isNew").getAsBoolean();

      GeoObjectOverTime go = GeoObjectOverTime.fromJSON(ServiceFactory.getAdapter(), geoObject);

      if (isNew)
      {
        go.setUid(ServiceFactory.getIdService().next());
        geoObject = go.toJSON().toString();

        this.editorService.createGeoObject(parentTreeNode, geoObject, null, null);
      }
      else
      {
        ServerGeoObjectIF serverGO = this.objectService.apply(go, isNew, true);
        final ServerGeoObjectType type = serverGO.getType();

        ServerParentTreeNodeOverTime ptnOt = ServerParentTreeNodeOverTime.fromJSON(type, parentTreeNode);

        this.objectService.setParents(serverGO, ptnOt);
      }

      err.appLock();
      err.setResolution(resolution);
      err.apply();

      hist.appLock();
      hist.setErrorResolvedCount(hist.getErrorResolvedCount() + 1);
      hist.setImportedRecords(hist.getImportedRecords() + 1);
      hist.apply();
    }
    else if (resolution.equals(ErrorResolution.IGNORE.name()))
    {
      err.appLock();
      err.setResolution(resolution);
      err.apply();
    }
    else
    {
      throw new UnsupportedOperationException("Invalid import resolution [" + resolution + "].");
    }
  }

  @Transaction
  public JsonObject submitValidationProblemResolution(String json)
  {
    JsonObject response = new JsonObject();

    JsonObject config = JsonParser.parseString(json).getAsJsonObject();

    ValidationProblem problem = ValidationProblem.get(config.get("validationProblemId").getAsString());

    ImportHistory hist = problem.getHistory();
    hist.getConfig().enforceExecutePermissions();

    String resolution = config.get("resolution").getAsString();

    if (resolution.equals(ValidationResolution.SYNONYM.name()))
    {
      if (problem instanceof TermReferenceProblem)
      {
        String classifierId = config.get("classifierId").getAsString();
        String label = config.get("label").getAsString();

        response = JsonParser.parseString(this.classifierService.createSynonym(classifierId, label)).getAsJsonObject();
      }
      else if (problem instanceof ParentReferenceProblem)
      {
        String code = config.get("code").getAsString();
        String typeCode = config.get("typeCode").getAsString();
        String label = config.get("label").getAsString();

        ServerGeoObjectIF go = this.objectService.getGeoObjectByCode(code, typeCode);

        response = JsonParser.parseString(this.geoSynonymService.createGeoEntitySynonym(typeCode, go.getCode(), label).toString()).getAsJsonObject();
      }

      problem.appLock();
      problem.setResolution(resolution);
      problem.apply();

      // hist.appLock();
      // hist.setErrorResolvedCount(hist.getErrorResolvedCount() + 1);
      // hist.apply();
    }
    else if (resolution.equals(ValidationResolution.IGNORE.name()))
    {
      problem.appLock();
      problem.setResolution(resolution);
      problem.apply();
    }
    else if (resolution.equals(ValidationResolution.CREATE.name()))
    {
      if (problem instanceof TermReferenceProblem)
      {
        String parentTermCode = config.get("parentTermCode").getAsString();
        String termJSON = config.get("termJSON").toString();

        response = this.termService.createTerm(parentTermCode, termJSON).toJSON();
      }
      else if (problem instanceof ParentReferenceProblem)
      {
        // TODO
      }

      problem.appLock();
      problem.setResolution(resolution);
      problem.apply();
    }
    else
    {
      throw new UnsupportedOperationException("Invalid import resolution [" + resolution + "].");
    }

    return response;
  }

  public void resolveImport(String historyId)
  {
    ImportHistory hist = ImportHistory.get(historyId);
    hist.getConfig().enforceExecutePermissions();

    if (hist.getStage().get(0).equals(ImportStage.IMPORT_RESOLVE))
    {
      resolveImportInTrans(historyId, hist);
    }
    else if (hist.getStage().get(0).equals(ImportStage.VALIDATION_RESOLVE))
    {
      this.doImport(hist.getConfigJson());
    }
  }

  @Transaction
  private void resolveImportInTrans(String historyId, ImportHistory hist)
  {
    hist.appLock();

    ImportErrorQuery ieq = new ImportErrorQuery(new QueryFactory());
    ieq.WHERE(ieq.getHistory().EQ(historyId));
    OIterator<? extends ImportError> it = ieq.getIterator();
    try
    {
      ImportError err = it.next();

      err.delete();
    }
    finally
    {
      it.close();
    }

    hist.clearStatus();
    hist.addStatus(AllJobStatus.SUCCESS);

    hist.clearStage();
    hist.addStage(ImportStage.COMPLETE);

    hist.apply();

    VaultFile file = hist.getImportFile();
    file.delete();
  }

}
