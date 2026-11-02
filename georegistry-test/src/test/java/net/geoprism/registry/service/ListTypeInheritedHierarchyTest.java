/**
 *
 */
package net.geoprism.registry.service;

import java.util.Date;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.google.gson.JsonObject;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.database.DuplicateDataDatabaseException;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;

import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.ListType;
import net.geoprism.registry.ListTypeEntry;
import net.geoprism.registry.ListTypeQuery;
import net.geoprism.registry.ListTypeVersion;
import net.geoprism.registry.InstanceTestClassExtension;
import net.geoprism.registry.USADatasetTest;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.service.business.GeoObjectTypeBusinessServiceIF;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.USATestData;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@ExtendWith(InstanceTestClassExtension.class)
public class ListTypeInheritedHierarchyTest extends USADatasetTest implements InstanceTestClassListener
{
  @Autowired
  private GeoObjectTypeBusinessServiceIF typeService;

  @Override
  public void beforeClassSetup() throws Exception
  {
    super.beforeClassSetup();

    setUpInReq();
  }

  @Request
  private void setUpInReq()
  {
    ServerGeoObjectType sGO = USATestData.DISTRICT.getServerObject();

    this.typeService.setInheritedHierarchy(sGO, USATestData.HIER_SCHOOL.getServerObject(), USATestData.HIER_ADMIN.getServerObject());
  }

  @BeforeEach
  public void setUp() throws Exception
  {
    super.setUp();

    cleanUpExtra();
  }

  @AfterEach
  public void tearDown() throws Exception
  {
    cleanUpExtra();

    super.tearDown();
  }

  @Request
  public void cleanUpExtra()
  {
    ListTypeQuery query = new ListTypeQuery(new QueryFactory());

    OIterator<? extends ListType> it = query.getIterator();

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
  }

  @Test
  public void testPublishVersion()
  {
    TestDataSet.executeRequestAsUser(USATestData.USER_ADMIN, () -> {

      JsonObject json = ListTypeTest.getJson(USATestData.ORG_NPS.getServerObject(), USATestData.HIER_SCHOOL, USATestData.SCHOOL_ZONE, USATestData.COUNTRY, USATestData.STATE, USATestData.DISTRICT);

      ListType test = ListType.apply(json);

      try
      {
        ListTypeEntry entry = test.getOrCreateEntry(new Date(), null);
        ListTypeVersion version = entry.getWorking();

        try
        {
          MdBusinessDAOIF mdTable = MdBusinessDAO.get(version.getMdBusinessOid());

          Assertions.assertNotNull(mdTable);

          version.publish();
        }
        finally
        {
          entry.delete();
        }
      }
      finally
      {
        test.delete();
      }
    });
  }

  @Test
  @Request
  public void testMarkAsInvalidByInheritedParent()
  {
    JsonObject json = ListTypeTest.getJson(USATestData.ORG_NPS.getServerObject(), USATestData.HIER_SCHOOL, USATestData.SCHOOL_ZONE, USATestData.DISTRICT, USATestData.STATE);

    ListType masterlist = ListType.apply(json);

    try
    {
      masterlist.markAsInvalid(USATestData.HIER_ADMIN.getServerObject(), USATestData.STATE.getServerObject());

      Assertions.assertFalse(masterlist.isValid());
    }
    catch (DuplicateDataDatabaseException e)
    {
      masterlist.delete();
    }
  }

}
