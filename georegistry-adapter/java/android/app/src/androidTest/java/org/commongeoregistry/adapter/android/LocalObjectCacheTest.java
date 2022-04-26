package org.commongeoregistry.adapter.android;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import org.commongeoregistry.adapter.HttpRegistryClient;
import org.commongeoregistry.adapter.MockIdService;
import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.action.AbstractActionDTO;
import org.commongeoregistry.adapter.action.geoobject.CreateGeoObjectActionDTO;
import org.commongeoregistry.adapter.action.geoobject.UpdateGeoObjectActionDTO;
import org.commongeoregistry.adapter.action.tree.AddChildActionDTO;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.ChildTreeNode;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.FrequencyType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.commongeoregistry.adapter.metadata.MetadataFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class LocalObjectCacheTest {
    public static String PROVINCE = "PROVINCE";

    public static String DISTRICT = "DISTRICT";

    public static String COMMUNE = "COMMUNE";

    public static String VILLAGE = "VILLAGE";

    public static String HOUSEHOLD = "HOUSEHOLD";

    public static String FOCUS_AREA = "FOCUS_AREA";

    public static String HEALTH_FACILITY = "HEALTH_FACILITY";

    public static String HEALTH_FACILITY_ATTRIBUTE = "healthFacilityType";

    public static String GEOPOLITICAL = "GEOPOLITICAL";

    public static String HEALTH_ADMINISTRATIVE = "HEALTH_ADMINISTRATIVE";

    private HttpRegistryClient client;

    private LocalObjectCache cache;
    private HierarchyType geoPolitical;

    @Before
    public void setup() {
        /*
         * Setup mock objects
         */
        MockHttpConnector connector = new MockHttpConnector();
        this.client = new HttpRegistryClient(connector, new MockIdService());

        Context context = InstrumentationRegistry.getTargetContext();

        this.cache = new LocalObjectCache(context, this.client);
        cache.clear();

        // Define GeoObject Types
        GeoObjectType province = MetadataFactory.newGeoObjectType(PROVINCE, GeometryType.POLYGON, new LocalizedValue("Province"), new LocalizedValue(""), false, false, FrequencyType.DAILY, client);

        GeoObjectType district = MetadataFactory.newGeoObjectType(DISTRICT, GeometryType.POLYGON, new LocalizedValue("District"), new LocalizedValue(""), false, false, FrequencyType.DAILY, client);

        GeoObjectType commune = MetadataFactory.newGeoObjectType(COMMUNE, GeometryType.POLYGON, new LocalizedValue("Commune"), new LocalizedValue(""), false, false, FrequencyType.DAILY, client);

        GeoObjectType village = MetadataFactory.newGeoObjectType(VILLAGE, GeometryType.POLYGON, new LocalizedValue("Village"), new LocalizedValue(""), false, false, FrequencyType.DAILY, client);

        GeoObjectType household = MetadataFactory.newGeoObjectType(HOUSEHOLD, GeometryType.POLYGON, new LocalizedValue("Household"), new LocalizedValue(""), false, false, FrequencyType.DAILY, client);

        GeoObjectType focusArea = MetadataFactory.newGeoObjectType(FOCUS_AREA, GeometryType.POLYGON, new LocalizedValue("Focus Area"), new LocalizedValue(""), false, false, FrequencyType.DAILY, client);

        GeoObjectType healthFacility = MetadataFactory.newGeoObjectType(HEALTH_FACILITY, GeometryType.POLYGON, new LocalizedValue("Health Facility"), new LocalizedValue(""), false, false, FrequencyType.DAILY, client);
        healthFacility.addAttribute(createHealthFacilityTypeAttribute(client));

        // Define Geopolitical Hierarchy Type
        this.geoPolitical = MetadataFactory.newHierarchyType(GEOPOLITICAL, new LocalizedValue("Geopolitical"), new LocalizedValue("Geopolitical Hierarchy"), client);
        HierarchyType.HierarchyNode geoProvinceNode = new HierarchyType.HierarchyNode(province);
        HierarchyType.HierarchyNode geoDistrictNode = new HierarchyType.HierarchyNode(district);
        HierarchyType.HierarchyNode geoCommuneNode = new HierarchyType.HierarchyNode(commune);
        HierarchyType.HierarchyNode geoVillageNode = new HierarchyType.HierarchyNode(village);
        HierarchyType.HierarchyNode geoHouseholdNode = new HierarchyType.HierarchyNode(household);

        geoProvinceNode.addChild(geoDistrictNode);
        geoDistrictNode.addChild(geoCommuneNode);
        geoCommuneNode.addChild(geoVillageNode);
        geoVillageNode.addChild(geoHouseholdNode);

        geoPolitical.addRootGeoObjects(geoProvinceNode);
    }

    private AttributeTermType createHealthFacilityTypeAttribute(HttpRegistryClient client) {
        AttributeTermType attrType =
                (AttributeTermType) AttributeType.factory(HEALTH_FACILITY_ATTRIBUTE, new LocalizedValue("Health Facility Type"), new LocalizedValue("The type of health facility"), AttributeTermType.TYPE, false, false, true);

        Term rootTerm = createHealthFacilityTerms(client);

        attrType.setRootTerm(rootTerm);

        return attrType;
    }

    private static Term createHealthFacilityTerms(HttpRegistryClient registry) {
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

    public GeoObjectOverTime generateGeoObject(String genKey, String typeCode)
    {
        GeoObjectOverTime geoObject = client.newGeoObjectOverTimeInstance(typeCode);

        String geom = "MULTIPOLYGON (((10000 10000, 12300 40000, 16800 50000, 12354 60000, 13354 60000, 17800 50000, 13300 40000, 11000 10000, 10000 10000)))";

        geoObject.setWKTGeometry(geom, null);
        geoObject.setCode(genKey + "_CODE");
        geoObject.setUid(genKey + "_UID");
        geoObject.setDisplayLabel(new LocalizedValue(genKey + " Display Label"), null, null);

        return geoObject;
    }

    @Test
    public void testCacheAndGetGeoObject() {

        /*
         * Setup mock objects
         */
        GeoObject geoObject = client.newGeoObjectInstance(DISTRICT);
        geoObject.setCode("Test");
        geoObject.setUid("state1");

        try {
            cache.cache(geoObject);

            GeoObject test = cache.getGeoObject(geoObject.getUid());

            Assert.assertNotNull(test);
        } finally {
            cache.close();
        }
    }

    @Test
    public void testCacheAndGetChildGeoObjects() {

        /*
         * Setup mock objects
         */
        GeoObject pOne = client.newGeoObjectInstance(PROVINCE);
        pOne.setCode("pOne");
        pOne.setUid("pOne");
        ChildTreeNode ptOne = new ChildTreeNode(pOne, geoPolitical);

        GeoObject dOne = client.newGeoObjectInstance(DISTRICT);
        dOne.setCode("dOne");
        dOne.setUid("dOne");
        ChildTreeNode dtOne = new ChildTreeNode(dOne, geoPolitical);
        ptOne.addChild(dtOne);

        GeoObject cOne = client.newGeoObjectInstance(COMMUNE);
        cOne.setCode("cOne");
        cOne.setUid("cOne");
        ChildTreeNode ctOne = new ChildTreeNode(cOne, geoPolitical);
        dtOne.addChild(ctOne);

        GeoObject dTwo = client.newGeoObjectInstance(DISTRICT);
        dTwo.setCode("dTwo");
        dTwo.setUid("dTwo");
        ChildTreeNode dtTwo = new ChildTreeNode(dTwo, geoPolitical);
        ptOne.addChild(dtTwo);

        try {
            cache.cache(ptOne);

            ChildTreeNode test = cache.getChildGeoObjects(pOne.getUid(), new String[]{geoPolitical.getCode()}, false);

            Assert.assertNotNull(test);

            Assert.assertEquals(pOne.getUid(), test.getGeoObject().getUid());

            List<ChildTreeNode> children = test.getChildren();

            Assert.assertEquals(2, children.size());

            ChildTreeNode tPtOne = children.get(0);

            Assert.assertEquals(dOne.getUid(), tPtOne.getGeoObject().getUid());

            children = tPtOne.getChildren();

            Assert.assertEquals(0, children.size());
        } finally {
            cache.close();
        }
    }

    @Test
    public void testGetChildGeoObjectsBadHierarchy() {

        /*
         * Setup mock objects
         */
        GeoObject pOne = client.newGeoObjectInstance(PROVINCE);
        pOne.setCode("pOne");
        pOne.setUid("pOne");
        ChildTreeNode ptOne = new ChildTreeNode(pOne, geoPolitical);

        GeoObject dOne = client.newGeoObjectInstance(DISTRICT);
        dOne.setCode("dOne");
        dOne.setUid("dOne");
        ChildTreeNode dtOne = new ChildTreeNode(dOne, geoPolitical);
        ptOne.addChild(dtOne);

        GeoObject cOne = client.newGeoObjectInstance(COMMUNE);
        cOne.setCode("cOne");
        cOne.setUid("cOne");
        ChildTreeNode ctOne = new ChildTreeNode(cOne, geoPolitical);
        dtOne.addChild(ctOne);

        GeoObject dTwo = client.newGeoObjectInstance(DISTRICT);
        dTwo.setCode("dTwo");
        dTwo.setUid("dTwo");
        ChildTreeNode dtTwo = new ChildTreeNode(dTwo, geoPolitical);
        ptOne.addChild(dtTwo);

        try {
            cache.cache(ptOne);

            ChildTreeNode test = cache.getChildGeoObjects(pOne.getUid(), new String[]{"NONE"}, false);

            Assert.assertNotNull(test);

            Assert.assertEquals(pOne.getUid(), test.getGeoObject().getUid());

            List<ChildTreeNode> children = test.getChildren();

            Assert.assertEquals(0, children.size());
        } finally {
            cache.close();
        }
    }


    @Test
    public void testGetChildGeoObjectsRecursive() {

        /*
         * Setup mock objects
         */
        GeoObject pOne = client.newGeoObjectInstance(PROVINCE);
        pOne.setCode("pOne");
        pOne.setUid("pOne");
        ChildTreeNode ptOne = new ChildTreeNode(pOne, geoPolitical);

        GeoObject dOne = client.newGeoObjectInstance(DISTRICT);
        dOne.setCode("dOne");
        dOne.setUid("dOne");
        ChildTreeNode dtOne = new ChildTreeNode(dOne, geoPolitical);
        ptOne.addChild(dtOne);

        GeoObject cOne = client.newGeoObjectInstance(COMMUNE);
        cOne.setCode("cOne");
        cOne.setUid("cOne");
        ChildTreeNode ctOne = new ChildTreeNode(cOne, geoPolitical);
        dtOne.addChild(ctOne);

        GeoObject dTwo = client.newGeoObjectInstance(DISTRICT);
        dTwo.setCode("dTwo");
        dTwo.setUid("dTwo");
        ChildTreeNode dtTwo = new ChildTreeNode(dTwo, geoPolitical);
        ptOne.addChild(dtTwo);

        try {
            cache.cache(ptOne);

            ChildTreeNode test = cache.getChildGeoObjects(pOne.getUid(), new String[]{geoPolitical.getCode()}, true);

            Assert.assertNotNull(test);

            Assert.assertEquals(pOne.getUid(), test.getGeoObject().getUid());

            List<ChildTreeNode> children = test.getChildren();

            Assert.assertEquals(2, children.size());

            ChildTreeNode tPtOne = children.get(0);

            Assert.assertEquals(dOne.getUid(), tPtOne.getGeoObject().getUid());

            children = tPtOne.getChildren();

            Assert.assertEquals(1, children.size());

            ChildTreeNode tDtOne = children.get(0);

            Assert.assertEquals(cOne.getUid(), tDtOne.getGeoObject().getUid());
        } finally {
            cache.close();
        }
    }

    @Test
    public void testGetParentGeoObjects() {

        /*
         * Setup mock objects
         */
        GeoObject pOne = client.newGeoObjectInstance(PROVINCE);
        pOne.setCode("pOne");
        pOne.setUid("pOne");
        ChildTreeNode ptOne = new ChildTreeNode(pOne, geoPolitical);

        GeoObject dOne = client.newGeoObjectInstance(DISTRICT);
        dOne.setCode("dOne");
        dOne.setUid("dOne");
        ChildTreeNode dtOne = new ChildTreeNode(dOne, geoPolitical);
        ptOne.addChild(dtOne);

        GeoObject cOne = client.newGeoObjectInstance(COMMUNE);
        cOne.setCode("cOne");
        cOne.setUid("cOne");
        ChildTreeNode ctOne = new ChildTreeNode(cOne, geoPolitical);
        dtOne.addChild(ctOne);

        GeoObject dTwo = client.newGeoObjectInstance(DISTRICT);
        dTwo.setCode("dTwo");
        dTwo.setUid("dTwo");
        ChildTreeNode dtTwo = new ChildTreeNode(dTwo, geoPolitical);
        ptOne.addChild(dtTwo);

        try {
            cache.cache(ptOne);

            ParentTreeNode test = cache.getParentGeoObjects(cOne.getUid(), new String[]{geoPolitical.getCode()}, false);

            Assert.assertNotNull(test);

            Assert.assertEquals(cOne.getUid(), test.getGeoObject().getUid());

            List<ParentTreeNode> parents = test.getParents();

            Assert.assertEquals(1, parents.size());

            ParentTreeNode tDtOne = parents.get(0);

            Assert.assertEquals(dOne.getUid(), tDtOne.getGeoObject().getUid());

            parents = tDtOne.getParents();

            Assert.assertEquals(0, parents.size());
        } finally {
            cache.close();
        }
    }

    @Test
    public void testGetParentGeoObjectsBadHierarchy() {

        /*
         * Setup mock objects
         */
        GeoObject pOne = client.newGeoObjectInstance(PROVINCE);
        pOne.setCode("pOne");
        pOne.setUid("pOne");
        ChildTreeNode ptOne = new ChildTreeNode(pOne, geoPolitical);

        GeoObject dOne = client.newGeoObjectInstance(DISTRICT);
        dOne.setCode("dOne");
        dOne.setUid("dOne");
        ChildTreeNode dtOne = new ChildTreeNode(dOne, geoPolitical);
        ptOne.addChild(dtOne);

        GeoObject cOne = client.newGeoObjectInstance(COMMUNE);
        cOne.setCode("cOne");
        cOne.setUid("cOne");
        ChildTreeNode ctOne = new ChildTreeNode(cOne, geoPolitical);
        dtOne.addChild(ctOne);

        GeoObject dTwo = client.newGeoObjectInstance(DISTRICT);
        dTwo.setCode("dTwo");
        dTwo.setUid("dTwo");
        ChildTreeNode dtTwo = new ChildTreeNode(dTwo, geoPolitical);
        ptOne.addChild(dtTwo);


        // Context of the app under test.

        try {
            cache.cache(ptOne);

            ParentTreeNode test = cache.getParentGeoObjects(dTwo.getUid(), new String[]{"NONE"}, false);

            Assert.assertNotNull(test);

            Assert.assertEquals(dTwo.getUid(), test.getGeoObject().getUid());

            List<ParentTreeNode> parents = test.getParents();

            Assert.assertEquals(0, parents.size());
        } finally {
            cache.close();
        }
    }


    @Test
    public void testCacheAndGetParentGeoObjectsRecursive() {

        /*
         * Setup mock objects
         */
        GeoObject pOne = client.newGeoObjectInstance(PROVINCE);
        pOne.setCode("pOne");
        pOne.setUid("pOne");
        ParentTreeNode ptOne = new ParentTreeNode(pOne, geoPolitical);

        GeoObject dOne = client.newGeoObjectInstance(DISTRICT);
        dOne.setCode("dOne");
        dOne.setUid("dOne");
        ParentTreeNode dtOne = new ParentTreeNode(dOne, geoPolitical);
        dtOne.addParent(ptOne);

        GeoObject cOne = client.newGeoObjectInstance(COMMUNE);
        cOne.setCode("cOne");
        cOne.setUid("cOne");
        ParentTreeNode ctOne = new ParentTreeNode(cOne, geoPolitical);
        ctOne.addParent(dtOne);

        try {
            cache.cache(ctOne);

            ParentTreeNode test = cache.getParentGeoObjects(cOne.getUid(), new String[]{geoPolitical.getCode()}, true);

            Assert.assertNotNull(test);

            Assert.assertEquals(cOne.getUid(), test.getGeoObject().getUid());

            List<ParentTreeNode> parents = test.getParents();

            Assert.assertEquals(1, parents.size());

            ParentTreeNode tDtOne = parents.get(0);

            Assert.assertEquals(dOne.getUid(), tDtOne.getGeoObject().getUid());

            parents = tDtOne.getParents();

            Assert.assertEquals(1, parents.size());

            ParentTreeNode tPtOne = parents.get(0);

            Assert.assertEquals(pOne.getUid(), tPtOne.getGeoObject().getUid());
        } finally {
            cache.close();
        }
    }

    @Test
    public void testCacheActions() {
        final int numActions = 3;

        GeoObjectOverTime geoObj1 = generateGeoObject("ActionTest1", PROVINCE);
        GeoObjectOverTime geoObj2 = generateGeoObject("ActionTest2", PROVINCE);
        GeoObjectType province = geoObj1.getType();
        HierarchyType geoPolitical = client.getMetadataCache().getHierachyType(GEOPOLITICAL).get();

        String action1GeoObj1 = geoObj1.toJSON().toString();
        cache.createGeoObjectOverTime(geoObj1);
        cache.createGeoObjectOverTime(geoObj2);
//        cache.addChild(geoObj1, geoObj2, geoPolitical); // TODO

        geoObj1.setCode("TEST_MODIFIED_CODE");
        cache.updateGeoObjectOverTime(geoObj1);

        List<AbstractActionDTO> actions = cache.getAllActionHistory();

        Assert.assertEquals(3, actions.size());

        Assert.assertEquals(action1GeoObj1,((CreateGeoObjectActionDTO)actions.get(0)).getGeoObject().toString());
        Assert.assertEquals(geoObj2.toJSON().toString(),((CreateGeoObjectActionDTO)actions.get(1)).getGeoObject().toString());

        // TODO : AddChild not yet possible due to quirks with value over time stuff
//        AddChildActionDTO aca = (AddChildActionDTO) actions.get(2);
//        Assert.assertEquals(geoObj1.getUid(), aca.getChildId());
//        Assert.assertEquals(geoObj2.getUid(), aca.getParentId());
//        Assert.assertEquals(geoPolitical.getCode(), aca.getHierarchyCode());
//        Assert.assertEquals(geoObj1.toJSON().toString(),((UpdateGeoObjectActionDTO)actions.get(3)).getGeoObject().toString());

        /*
         * Test the 'unpushed action history' method
         */
        Assert.assertEquals(numActions, cache.getUnpushedActionHistory().size());
        Assert.assertEquals(numActions, cache.getUnpushedActionHistory().size());
        cache.saveLastPushDate(new Date().getTime());
        Assert.assertEquals(0, cache.getUnpushedActionHistory().size());

        cache.updateGeoObjectOverTime(geoObj1);
        Assert.assertEquals(1, cache.getUnpushedActionHistory().size());
        cache.saveLastPushDate(new Date().getTime());
        Assert.assertEquals(0, cache.getUnpushedActionHistory().size());
    }

    @Test
    public void testCacheIds() {
        Assert.assertEquals(0, cache.countNumberRegistryIds());

        Collection<String> newIds = new HashSet<>();

        for (int i = 0; i < 100; ++i)
        {
            newIds.add(MockIdService.genId());
        }

        cache.addRegistryIds(newIds);
        Assert.assertEquals(100, cache.countNumberRegistryIds());

        Assert.assertTrue(newIds.contains(cache.nextRegistryId()));
        Assert.assertTrue(newIds.contains(cache.nextRegistryId()));
        Assert.assertTrue(newIds.contains(cache.nextRegistryId()));
        Assert.assertTrue(newIds.contains(cache.nextRegistryId()));

        cache.clear();
        Assert.assertEquals(0, cache.countNumberRegistryIds());

        newIds.clear();
        for (int i = 0; i < 50; ++i)
        {
            newIds.add(MockIdService.genId());
        }

        cache.addRegistryIds(newIds);
        Assert.assertEquals(50, cache.countNumberRegistryIds());
    }
}
