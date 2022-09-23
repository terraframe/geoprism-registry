package net.geoprism.registry.etl.dhis2;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeLocalType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.junit.Assert;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.geoprism.dhis2.dhis2adapter.DHIS2Objects;
import net.geoprism.registry.etl.DHIS2AttributeMapping;
import net.geoprism.registry.etl.DHIS2OrgUnitGroupAttributeMapping;
import net.geoprism.registry.etl.export.dhis2.DHIS2GeoObjectJsonAdapters;
import net.geoprism.registry.test.AllAttributesDataset;
import net.geoprism.registry.test.TestAttributeTypeInfo;
import net.geoprism.registry.test.TestGeoObjectInfo;

public class DHIS2PayloadValidator
{
  public static void orgUnit(TestGeoObjectInfo go, TestAttributeTypeInfo attr, DHIS2AttributeMapping mapping, int level, JsonObject joPayload)
  {
    JsonArray orgUnits = joPayload.get("organisationUnits").getAsJsonArray();

    Assert.assertEquals(1, orgUnits.size());

    JsonObject orgUnit = orgUnits.get(0).getAsJsonObject();

    Assert.assertEquals(level, orgUnit.get("level").getAsInt());

    Assert.assertEquals("MULTI_POLYGON", orgUnit.get("featureType").getAsString());

    if (level == 0)
    {
      Assert.assertEquals(AllAttributesDataset.GO_ALL.getCode(), orgUnit.get("code").getAsString());
    }
    else
    {
      Assert.assertEquals(go.getCode(), orgUnit.get("code").getAsString());

      Assert.assertTrue(orgUnit.has("attributeValues"));

      JsonArray attributeValues = orgUnit.get("attributeValues").getAsJsonArray();
      
      if (!(mapping instanceof DHIS2OrgUnitGroupAttributeMapping))
      {
        Assert.assertEquals(1, attributeValues.size());
 
        JsonObject attributeValue = attributeValues.get(0).getAsJsonObject();
 
        Assert.assertNotNull(attributeValue.get("lastUpdated").getAsString());
 
        Assert.assertNotNull(attributeValue.get("created").getAsString());
 
        AttributeType attrDto = attr.fetchDTO();
 
        if (attrDto instanceof AttributeIntegerType)
        {
          Assert.assertEquals(go.getServerObject().getValue(attr.getAttributeName()), attributeValue.get("value").getAsLong());
        }
        else if (attrDto instanceof AttributeFloatType)
        {
          Assert.assertEquals(go.getServerObject().getValue(attr.getAttributeName()), attributeValue.get("value").getAsDouble());
        }
        else if (attrDto instanceof AttributeDateType)
        {
          // TODO : If we fetch the object from the database in this manner the
          // miliseconds aren't included on the date. But if we fetch the object
          // via a query (as in DataExportJob) then the miliseconds ARE
          // included...
          // String expected =
          // DHIS2GeoObjectJsonAdapters.DHIS2Serializer.formatDate((Date)
          // go.getServerObject().getValue(attr.getAttributeName()));
 
          String expected = DHIS2GeoObjectJsonAdapters.DHIS2Serializer.formatDate(AllAttributesDataset.GO_DATE_VALUE);
          String actual = attributeValue.get("value").getAsString();
 
          Assert.assertEquals(expected, actual);
        }
        else if (attrDto instanceof AttributeBooleanType)
        {
          Assert.assertEquals(go.getServerObject().getValue(attr.getAttributeName()), attributeValue.get("value").getAsBoolean());
        }
        else if (attrDto instanceof AttributeTermType)
        {
          String dhis2Id = attributeValue.get("value").getAsString();
 
          // Term term = (Term)
          // go.getServerObject().getValue(attr.getAttributeName());
 
          Assert.assertEquals("TEST_EXTERNAL_ID", dhis2Id);
        }
        else if (attrDto instanceof AttributeLocalType)
        {
          Assert.assertEquals(AllAttributesDataset.GO_LOCAL.getDefaultValue(AllAttributesDataset.AT_GO_LOCAL.getAttributeName()), attributeValue.get("value").getAsString());
        }
        else
        {
          Assert.assertEquals(go.getServerObject().getValue(attr.getAttributeName()), attributeValue.get("value").getAsString());
        }
 
        Assert.assertEquals("TEST_EXTERNAL_ID", attributeValue.get("attribute").getAsJsonObject().get("id").getAsString());
      }
      else
      {
        Assert.assertEquals(0, attributeValues.size());
      }
    }
  }
  
  public static void orgUnitGroup(TestGeoObjectInfo go, TestAttributeTypeInfo attr, DHIS2OrgUnitGroupAttributeMapping mapping, int level, JsonObject joPayload)
  {
    JsonArray groups = joPayload.get(DHIS2Objects.ORGANISATION_UNIT_GROUPS).getAsJsonArray();
    
    Set<String> payloadIds = new HashSet<String>();
    
    for (int i = 0; i < groups.size(); ++i)
    {
      JsonObject joGroup = groups.get(i).getAsJsonObject();
      
      payloadIds.add(joGroup.get("id").getAsString());
    }
    
    Map<String, String> termMappings = mapping.getTerms();
    
    for (String expectedId : termMappings.values())
    {
      Assert.assertTrue("expected id " + expectedId + " to exist in payloadIds [" + StringUtils.join(payloadIds, ", ") + "]", payloadIds.contains(expectedId));
    }
  }
}
