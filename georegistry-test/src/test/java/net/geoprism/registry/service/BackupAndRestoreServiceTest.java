package net.geoprism.registry.service;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.google.gson.JsonObject;
import com.runwaysdk.session.Request;

import net.geoprism.registry.BusinessEdgeType;
import net.geoprism.registry.BusinessType;
import net.geoprism.registry.DirectedAcyclicGraphType;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.TestConfig;
import net.geoprism.registry.USADatasetTest;
import net.geoprism.registry.UndirectedGraphType;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.graph.transition.Transition;
import net.geoprism.registry.graph.transition.Transition.TransitionImpact;
import net.geoprism.registry.graph.transition.Transition.TransitionType;
import net.geoprism.registry.graph.transition.TransitionEvent;
import net.geoprism.registry.model.BusinessObject;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.service.business.BackupAndRestoreServiceIF;
import net.geoprism.registry.service.business.BusinessEdgeTypeBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessObjectBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessTypeBusinessServiceIF;
import net.geoprism.registry.service.business.DirectedAcyclicGraphTypeBusinessServiceIF;
import net.geoprism.registry.service.business.GPRTransitionEventBusinessService;
import net.geoprism.registry.service.business.UndirectedGraphTypeBusinessServiceIF;
import net.geoprism.registry.service.request.ServiceFactory;
import net.geoprism.registry.test.USATestData;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class BackupAndRestoreServiceTest extends USADatasetTest
{
  protected static DirectedAcyclicGraphType         dagType;

  protected static UndirectedGraphType              ugType;

  private static BusinessType                       bType;

  private static BusinessEdgeType                   bEdgeType;

  private static BusinessObject                     boParent;

  private static BusinessObject                     boChild;

  private static TransitionEvent                    event;

  private static Transition                         transition;

  @Autowired
  private BackupAndRestoreServiceIF                 backupService;

  @Autowired
  private UndirectedGraphTypeBusinessServiceIF      ugTypeService;

  @Autowired
  private DirectedAcyclicGraphTypeBusinessServiceIF dagTypeService;

  @Autowired
  private BusinessTypeBusinessServiceIF             bTypeService;

  @Autowired
  private BusinessEdgeTypeBusinessServiceIF         bEdgeService;

  @Autowired
  private BusinessObjectBusinessServiceIF           bObjectService;

  @Autowired
  private GPRTransitionEventBusinessService         teService;

  @Override
  @Request
  public void beforeClassSetup() throws Exception
  {
    super.beforeClassSetup();

    dagType = this.dagTypeService.create("TEST_DAG", new LocalizedValue("TEST_DAG"), new LocalizedValue("TEST_DAG"));
    ugType = this.ugTypeService.create("TEST_UG", new LocalizedValue("TEST_UG"), new LocalizedValue("TEST_UG"));

    JsonObject object = new JsonObject();
    object.addProperty(BusinessType.CODE, "TEST_BO");
    object.addProperty(BusinessType.ORGANIZATION, USATestData.ORG_NPS.getCode());
    object.add(BusinessType.DISPLAYLABEL, new LocalizedValue("TEST_BO").toJSON());

    bType = this.bTypeService.apply(object);

    String code = "TEST_EDGE";
    LocalizedValue label = new LocalizedValue("Test Edge");
    LocalizedValue description = new LocalizedValue("Test Edge Description");

    bEdgeType = this.bEdgeService.create(USATestData.ORG_NPS.getCode(), code, label, description, bType.getCode(), bType.getCode());
  }

  @Override
  @Request
  public void afterClassSetup() throws Exception
  {
    try
    {

      if (dagType != null)
      {
        this.dagTypeService.delete(DirectedAcyclicGraphType.getByCode(dagType.getCode()));
      }

      if (ugType != null)
      {
        this.ugTypeService.delete(UndirectedGraphType.getByCode(ugType.getCode()));
      }

      if (bEdgeType != null)
      {
        this.bEdgeService.delete(this.bEdgeService.getByCode(bEdgeType.getCode()));
      }

      if (bType != null)
      {
        this.bTypeService.delete(this.bTypeService.getByCode(bType.getCode()));
      }
    }
    catch (Exception e)
    {

    }

    super.afterClassSetup();
  }

  @Before
  @Request
  public void setUp()
  {
    testData.setUpInstanceData();

    testData.logIn(USATestData.USER_NPS_RA);

    ServerGeoObjectIF child = USATestData.CANADA.getServerObject();

    child.addGraphParent(USATestData.USA.getServerObject(), dagType, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_OVER_TIME_DATE, true);
    child.addGraphParent(USATestData.MEXICO.getServerObject(), ugType, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_OVER_TIME_DATE, true);

    boParent = this.bObjectService.newInstance(bType);
    boParent.setCode("BoParent");
    this.bObjectService.apply(boParent);

    boChild = this.bObjectService.newInstance(bType);
    boChild.setCode("BoChild");
    this.bObjectService.apply(boChild);

    this.bObjectService.addChild(boParent, bEdgeType, boChild);

    TransitionEvent event = new TransitionEvent();

    LocalizedValueConverter.populate(event, TransitionEvent.DESCRIPTION, new LocalizedValue("Test"));
    event.setEventDate(USATestData.DEFAULT_OVER_TIME_DATE);
    event.setBeforeTypeCode(USATestData.DISTRICT.getCode());
    event.setBeforeTypeOrgCode(USATestData.DISTRICT.getOrganization().getCode());
    event.setAfterTypeCode(USATestData.STATE.getCode());
    event.setAfterTypeOrgCode(USATestData.STATE.getOrganization().getCode());

    teService.apply(event);

    transition = teService.addTransition(event, USATestData.CO_D_ONE.getServerObject(), USATestData.COLORADO.getServerObject(), TransitionType.UPGRADE, TransitionImpact.FULL);
  }

  @After
  @Request
  public void tearDown()
  {
    try
    {
      if (event != null)
      {
        this.teService.delete(event);
      }

      if (boChild != null)
      {
        this.bObjectService.delete(boChild);
      }

      if (boParent != null)
      {
        this.bObjectService.delete(boParent);
      }
    }
    catch (Exception e)
    {

    }

    testData.logOut();

    testData.tearDownInstanceData();
  }

  @Request
  @Test
  public void testImportAndExport() throws IOException
  {
    try
    {

      File file = File.createTempFile("gpr-dump", ".zip");

      try
      {
        this.backupService.createBackup(file);

        Assert.assertTrue(file.exists());

        this.backupService.deleteData();

        this.backupService.restoreFromBackup(file);

      }
      finally
      {
        FileUtils.deleteQuietly(file);
      }
    }
    finally
    {
      ServiceFactory.getGraphRepoService().refreshMetadataCache();
    }
  }

}
