/**
 *
 */
package net.geoprism.registry.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeClassificationType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.session.Request;

import net.geoprism.registry.ConceptDatasetTest;
import net.geoprism.registry.InstanceTestClassExtension;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.graph.BusinessType;
import net.geoprism.registry.model.BusinessObject;
import net.geoprism.registry.model.ConceptObject;
import net.geoprism.registry.service.business.BackupSection;
import net.geoprism.registry.service.business.BackupService;
import net.geoprism.registry.service.business.BackupServiceIF;
import net.geoprism.registry.service.business.BusinessObjectBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessTypeBusinessServiceIF;
import net.geoprism.registry.service.business.RestoreServiceIF;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestOrganizationInfo;
import net.geoprism.registry.test.USATestData;
import net.geoprism.registry.view.BusinessTypeDTO;
import net.geoprism.registry.view.ObjectOverTimeDTO;

/**
 * Tests that classification data survives a backup and restore: the terms of
 * the concept classes, the edges between them, and classification values on
 * business objects.
 *
 * Unlike BackupAndRestoreServiceTest these tests don't delete all of the data.
 * They back up a section, delete only the objects under test, and restore from
 * a zip holding only the files of those objects.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@ExtendWith(InstanceTestClassExtension.class)
public class BackupClassificationDataTest extends ConceptDatasetTest implements InstanceTestClassListener
{
  private static final String             BUSINESS_TYPE_CODE = "TEST_BACKUP_BO";

  private static final String             BUSINESS_OBJECT    = "TEST_BACKUP_OBJECT";

  @Autowired
  private BackupServiceIF                 backupService;

  @Autowired
  private RestoreServiceIF                restoreService;

  @Autowired
  private BusinessTypeBusinessServiceIF   bTypeService;

  @Autowired
  private BusinessObjectBusinessServiceIF bObjectService;

  @Override
  protected TestOrganizationInfo getOrganization()
  {
    return USATestData.ORG_NPS;
  }

  @Override
  @Request
  public void beforeClassSetup() throws Exception
  {
    USATestData.ORG_NPS.apply();

    super.beforeClassSetup();
  }

  @Override
  @Request
  public void afterClassSetup() throws Exception
  {
    super.afterClassSetup();

    USATestData.ORG_NPS.delete();
  }

  @Test
  @Request
  public void testConceptRoundTrip() throws Exception
  {
    File backup = File.createTempFile("gpr-backup", ".zip");
    File concepts = File.createTempFile("gpr-concepts", ".zip");

    try
    {
      JsonElement label = getDisplayLabel(childConcept);

      this.backupService.createBackup(backup, EnumSet.of(BackupSection.METADATA));

      // The terms and the edges between them are written with the metadata
      JsonArray objects = readJsonArray(backup, BackupService.CONCEPT_OBJECTS_FOLDER + "/" + CONCEPT_CLASS_CODE + ".json");

      Set<String> codes = new HashSet<String>();
      objects.forEach(element -> codes.add(element.getAsJsonObject().get("code").getAsString()));

      Assertions.assertEquals(Set.of(ROOT_CONCEPT, PARENT_CONCEPT, CHILD_CONCEPT), codes);

      JsonArray edges = readJsonArray(backup, BackupService.CONCEPT_EDGES_FOLDER + "/" + CONCEPT_EDGE_CODE + ".json");

      Assertions.assertEquals(2, edges.size());

      JsonObject edge = edges.get(0).getAsJsonObject();

      Assertions.assertEquals(CONCEPT_CLASS_CODE, edge.get("sourceType").getAsString());
      Assertions.assertEquals(CONCEPT_CLASS_CODE, edge.get("targetType").getAsString());

      // Delete the terms (which deletes their edges) and restore only the
      // concept data
      this.cObjectService.delete(childConcept);
      this.cObjectService.delete(parentConcept);
      this.cObjectService.delete(rootConcept);

      childConcept = null;
      parentConcept = null;
      rootConcept = null;

      copyEntries(backup, concepts, name -> name.startsWith(BackupService.CONCEPT_OBJECTS_FOLDER + "/") || name.startsWith(BackupService.CONCEPT_EDGES_FOLDER + "/"));

      try (InputStream istream = new FileInputStream(concepts))
      {
        this.restoreService.restoreFromBackup(istream);
      }

      rootConcept = this.cObjectService.getByCode(cClass, ROOT_CONCEPT).orElse(null);
      parentConcept = this.cObjectService.getByCode(cClass, PARENT_CONCEPT).orElse(null);
      childConcept = this.cObjectService.getByCode(cClass, CHILD_CONCEPT).orElse(null);

      Assertions.assertNotNull(rootConcept);
      Assertions.assertNotNull(parentConcept);
      Assertions.assertNotNull(childConcept);

      // The change over time label is restored with its dates
      Assertions.assertEquals(label, getDisplayLabel(childConcept));

      // The hierarchy is restored
      List<ConceptObject> children = this.cObjectService.getChildren(rootConcept, cEdgeType, TestDataSet.DEFAULT_OVER_TIME_DATE);

      Assertions.assertEquals(1, children.size());
      Assertions.assertEquals(PARENT_CONCEPT, children.get(0).getCode());

      children = this.cObjectService.getChildren(parentConcept, cEdgeType, TestDataSet.DEFAULT_OVER_TIME_DATE);

      Assertions.assertEquals(1, children.size());
      Assertions.assertEquals(CHILD_CONCEPT, children.get(0).getCode());
    }
    finally
    {
      FileUtils.deleteQuietly(backup);
      FileUtils.deleteQuietly(concepts);
    }
  }

  @Test
  @Request
  public void testBusinessObjectClassificationRoundTrip() throws Exception
  {
    BusinessTypeDTO typeDTO = new BusinessTypeDTO();
    typeDTO.setCode(BUSINESS_TYPE_CODE);
    typeDTO.setOrganization(USATestData.ORG_NPS.getCode());
    typeDTO.setDisplayLabel(new LocalizedValue("Test Backup"));

    BusinessType type = this.bTypeService.apply(typeDTO);

    File backup = File.createTempFile("gpr-backup", ".zip");
    File objects = File.createTempFile("gpr-objects", ".zip");

    try
    {
      AttributeClassificationType attribute = this.createAttributeClassificationType();
      attribute.setChangeOverTime(false);
      attribute = (AttributeClassificationType) this.bTypeService.createAttributeType(type, attribute);

      BusinessObject object = this.bObjectService.newInstance(type);
      object.setCode(BUSINESS_OBJECT);
      object.setValue(attribute.getCode(), childConcept.getVertex());

      this.bObjectService.apply(object);

      this.backupService.createBackup(backup, EnumSet.of(BackupSection.BUSINESS_OBJECTS));

      // Delete the object and restore it from a zip with only its type's file
      this.bObjectService.delete(object);

      Assertions.assertFalse(this.bObjectService.getByCode(type, BUSINESS_OBJECT).isPresent());

      String path = "business-objects/" + BUSINESS_TYPE_CODE + ".json";

      copyEntries(backup, objects, name -> name.equals(path));

      try (InputStream istream = new FileInputStream(objects))
      {
        this.restoreService.restoreFromBackup(istream);
      }

      BusinessObject restored = this.bObjectService.getByCode(type, BUSINESS_OBJECT).orElse(null);

      Assertions.assertNotNull(restored);
      Assertions.assertEquals(childConcept.getOid(), restored.getValue(attribute.getCode()));

      this.bObjectService.delete(restored);
    }
    finally
    {
      FileUtils.deleteQuietly(backup);
      FileUtils.deleteQuietly(objects);

      this.bTypeService.delete(type);
    }
  }

  private JsonElement getDisplayLabel(ConceptObject object)
  {
    ObjectOverTimeDTO dto = this.cObjectService.toDTO(object);

    JsonObject json = JsonParser.parseString(ObjectOverTimeDTO.toJson(dto)).getAsJsonObject();

    JsonObject label = json.getAsJsonObject("properties").getAsJsonObject(DefaultAttribute.DISPLAY_LABEL.getName());

    // The oid of each value over time is regenerated when the object is
    // restored, so only the values and their dates are compared
    label.getAsJsonArray("values").forEach(value -> value.getAsJsonObject().remove("oid"));

    return label;
  }

  private JsonArray readJsonArray(File zipfile, String name) throws IOException
  {
    try (ZipFile zip = new ZipFile(zipfile))
    {
      ZipEntry entry = zip.getEntry(name);

      Assertions.assertNotNull(entry, "The backup is missing " + name);

      try (InputStreamReader reader = new InputStreamReader(zip.getInputStream(entry), StandardCharsets.UTF_8))
      {
        return JsonParser.parseReader(reader).getAsJsonArray();
      }
    }
  }

  private void copyEntries(File source, File target, Predicate<String> filter) throws IOException
  {
    try (ZipFile zip = new ZipFile(source); ZipOutputStream ostream = new ZipOutputStream(new FileOutputStream(target)))
    {
      for (ZipEntry entry : zip.stream().filter(e -> !e.isDirectory() && filter.test(e.getName())).toList())
      {
        ostream.putNextEntry(new ZipEntry(entry.getName()));

        try (InputStream istream = zip.getInputStream(entry))
        {
          istream.transferTo(ostream);
        }

        ostream.closeEntry();
      }
    }
  }
}
