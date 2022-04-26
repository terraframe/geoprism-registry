/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Common Geo Registry Adapter(tm).
 *
 * Common Geo Registry Adapter(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Common Geo Registry Adapter(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Common Geo Registry Adapter(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package org.commongeoregistry.adapter;

import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyNode;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.commongeoregistry.adapter.metadata.MetadataFactory;

public class TestFixture
{
  public static String PROVINCE                  = "PROVINCE";

  public static String DISTRICT                  = "DISTRICT";

  public static String COMMUNE                   = "COMMUNE";

  public static String VILLAGE                   = "VILLAGE";

  public static String HOUSEHOLD                 = "HOUSEHOLD";

  public static String FOCUS_AREA                = "FOCUS_AREA";

  public static String HEALTH_FACILITY           = "HEALTH_FACILITY";

  public static String HEALTH_FACILITY_ATTRIBUTE = "healthFacilityType";

  public static String GEOPOLITICAL              = "GEOPOLITICAL";

  public static String HEALTH_ADMINISTRATIVE     = "HEALTH_ADMINISTRATIVE";

  public static void defineExampleHierarchies(RegistryAdapter registry)
  {
    // Define GeoObject Types
    GeoObjectType province = MetadataFactory.newGeoObjectType(PROVINCE, GeometryType.POLYGON, new LocalizedValue("Province"), new LocalizedValue(""), true, null, registry);

    GeoObjectType district = MetadataFactory.newGeoObjectType(DISTRICT, GeometryType.POLYGON, new LocalizedValue("District"), new LocalizedValue(""), true, null, registry);

    GeoObjectType commune = MetadataFactory.newGeoObjectType(COMMUNE, GeometryType.POLYGON, new LocalizedValue("Commune"), new LocalizedValue(""), true, null, registry);

    GeoObjectType village = MetadataFactory.newGeoObjectType(VILLAGE, GeometryType.POLYGON, new LocalizedValue("Village"), new LocalizedValue(""), true, null, registry);

    GeoObjectType household = MetadataFactory.newGeoObjectType(HOUSEHOLD, GeometryType.POLYGON, new LocalizedValue("Household"), new LocalizedValue(""), true, null, registry);

    // GeoObjectType focusArea = MetadataFactory.newGeoObjectType(FOCUS_AREA,
    // GeometryType.POLYGON, "Focus Area", "", false, registry);

    GeoObjectType healthFacility = MetadataFactory.newGeoObjectType(HEALTH_FACILITY, GeometryType.POLYGON, new LocalizedValue("Health Facility"), new LocalizedValue(""), true, null, registry);

    healthFacility.addAttribute(createHealthFacilityTypeAttribute(registry));

    String organizationCode = "";

    // Define Geopolitical Hierarchy Type
    HierarchyType geoPolitical = MetadataFactory.newHierarchyType(GEOPOLITICAL, new LocalizedValue("Geopolitical"), new LocalizedValue("Geopolitical Hierarchy"), organizationCode, registry);
    geoPolitical.setAccessConstraints("Test Access");
    geoPolitical.setUseConstraints("Test use");
    geoPolitical.setAcknowledgement("Test acknowledgement");
    geoPolitical.setDisclaimer("Test disclaimer");
    geoPolitical.setContact("Test contact");
    geoPolitical.setPhoneNumber("Test phone number");
    geoPolitical.setEmail("Test email");

    HierarchyNode geoProvinceNode = new HierarchyNode(province);
    HierarchyNode geoDistrictNode = new HierarchyNode(district);
    HierarchyNode geoCommuneNode = new HierarchyNode(commune);
    HierarchyNode geoVillageNode = new HierarchyNode(village);
    HierarchyNode geoHouseholdNode = new HierarchyNode(household);

    geoProvinceNode.addChild(geoDistrictNode);
    geoDistrictNode.addChild(geoCommuneNode);
    geoCommuneNode.addChild(geoVillageNode);
    geoVillageNode.addChild(geoHouseholdNode);

    geoPolitical.addRootGeoObjects(geoProvinceNode);

    // Define Health Administrative
    HierarchyType healthAdministrative = MetadataFactory.newHierarchyType(TestFixture.HEALTH_ADMINISTRATIVE, new LocalizedValue("Health Administrative"), new LocalizedValue("Health Administrative Hierarchy"), organizationCode, registry);
    HierarchyNode healthProvinceNode = new HierarchyNode(province);
    HierarchyNode healthDistrictNode = new HierarchyNode(district);
    HierarchyNode healthCommuneNode = new HierarchyNode(commune);
    HierarchyNode healthFacilityNode = new HierarchyNode(healthFacility);

    healthProvinceNode.addChild(healthDistrictNode);
    healthDistrictNode.addChild(healthCommuneNode);
    healthCommuneNode.addChild(healthFacilityNode);

    healthAdministrative.addRootGeoObjects(healthProvinceNode);
  }

  public static GeoObject createGeoObject(RegistryAdapter registry, String genKey, String typeCode)
  {
    GeoObject geoObject = registry.newGeoObjectInstance(typeCode);

    String geom = "POLYGON ((10000 10000, 12300 40000, 16800 50000, 12354 60000, 13354 60000, 17800 50000, 13300 40000, 11000 10000, 10000 10000))";

    geoObject.setWKTGeometry(geom);
    geoObject.setCode(genKey + "_CODE");
    geoObject.setUid(genKey + "_UID");
    geoObject.getDisplayLabel().setValue(genKey + " Display Label");
    geoObject.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, genKey + " Display Label");

    return geoObject;
  }

  private static AttributeTermType createHealthFacilityTypeAttribute(RegistryAdapter registry)
  {
    AttributeTermType attrType = (AttributeTermType) AttributeType.factory(HEALTH_FACILITY_ATTRIBUTE, new LocalizedValue("Health Facility Type"), new LocalizedValue("The type of health facility"), AttributeTermType.TYPE, false, false, false);

    Term rootTerm = createHealthFacilityTerms(registry);

    attrType.setRootTerm(rootTerm);

    return attrType;
  }

  private static Term createHealthFacilityTerms(RegistryAdapter registry)
  {
    Term rootTerm = MetadataFactory.newTerm("CM:Health-Facility-Types", new LocalizedValue("Health Facility Types"), new LocalizedValue("The types of health facilities within a country"), registry);
    Term dispensary = MetadataFactory.newTerm("CM:Dispensary", new LocalizedValue("Dispensary"), new LocalizedValue(""), registry);
    Term privateClinic = MetadataFactory.newTerm("CM:Private-Clinic", new LocalizedValue("Private Clinic"), new LocalizedValue(""), registry);
    Term publicClinic = MetadataFactory.newTerm("CM:Public-Clinic", new LocalizedValue("Public Clinic"), new LocalizedValue(""), registry);
    Term matWard = MetadataFactory.newTerm("CM:Maternity-Ward", new LocalizedValue("Maternity Ward"), new LocalizedValue(""), registry);
    Term nursing = MetadataFactory.newTerm("CM:Nursing-Home", new LocalizedValue("Nursing Home"), new LocalizedValue(""), registry);

    rootTerm.addChild(dispensary);
    rootTerm.addChild(privateClinic);
    rootTerm.addChild(publicClinic);
    rootTerm.addChild(matWard);
    rootTerm.addChild(nursing);

    return rootTerm;
  }
}
