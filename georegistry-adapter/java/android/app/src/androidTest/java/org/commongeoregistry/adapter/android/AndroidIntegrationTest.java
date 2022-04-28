package org.commongeoregistry.adapter.android;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import junit.framework.Assert;

import org.commongeoregistry.adapter.android.framework.TestGeoObjectInfo;
import org.commongeoregistry.adapter.android.framework.USATestData;
import org.commongeoregistry.adapter.dataaccess.ChildTreeNode;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;
import org.commongeoregistry.adapter.http.AuthenticationException;
import org.commongeoregistry.adapter.http.ServerResponseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;

/**
 * Contains tests that run in Android and require a common geo registry server running.
 */
public class AndroidIntegrationTest
{
    public static final String serverUrl = "https://192.168.0.31:8443/georegistry";
//    public static final String serverUrl = "https://10.0.0.2:8443/georegistry";

    public static final String user = "admin";

    public static final String pass = "_nm8P4gfdWxGqNRQ#8";

    private USATestData data;

    private AndroidTestRegistryClient client;

    private TestGeoObjectInfo UTAH;

    private TestGeoObjectInfo CALIFORNIA;

    private TestGeoObjectInfo TEST_ADD_CHILD;

    @Before
    public void setUp() throws AuthenticationException, ServerResponseException, IOException {
        Context context = InstrumentationRegistry.getTargetContext();

        AndroidHttpCredentialConnector connector = new AndroidHttpCredentialConnector();
        connector.setCredentials(user, pass);
        connector.setServerUrl(serverUrl);
        connector.initialize();

        client = new AndroidTestRegistryClient(connector, context);
        client.getLocalCache().clear();
        client.testSetUp();

        client.refreshMetadataCache();
        client.getIdService().populate(500);

        data = new USATestData(client);
        // These objects are predefined:
        TEST_ADD_CHILD = data.newTestGeoObjectInfo("TEST_ADD_CHILD", data.DISTRICT);
        data.setUp();

        // These objects do not exist in the database (but tests will at some point create them):
        UTAH = data.newTestGeoObjectInfo("Utah", data.STATE);
        CALIFORNIA = data.newTestGeoObjectInfo("California", data.STATE);
    }

    @After
    public void cleanUp() throws AuthenticationException, ServerResponseException, IOException {
        client.testCleanUp();
    }

//    @Test(expected = InvalidLoginException.class)
//    public void testInvalidLoginException()
//    {
//        AndroidHttpCredentialConnector connector = new AndroidHttpCredentialConnector();
//        connector.setCredentials("admin", "bad");
//        connector.setServerUrl("https://192.168.0.23:8443/georegistry");
//        connector.initialize();
//
//        AndroidRegistryClient client = new AndroidRegistryClient(connector, InstrumentationRegistry.getTargetContext());
//        client.refreshMetadataCache();
//    }

    @Test
    public void testCreateGetUpdateGeoObject() throws AuthenticationException, ServerResponseException, IOException {
        // 1. Create a Geo Object locally
        GeoObject goUtah = UTAH.newGeoObject();

        // 2. Send the new GeoObject to the server to be applied to the database
        GeoObject go2 = client.createGeoObject(goUtah);
        UTAH.setUid(go2.getUid());
        UTAH.assertEquals(go2);

        // 3. Retrieve the new GeoObject from the server
        int numRegistryIds = client.getLocalCache().countNumberRegistryIds();
        GeoObject go3 = client.getGeoObject(go2.getUid(), go2.getType().getCode());
        UTAH.assertEquals(go3);
        Assert.assertEquals(numRegistryIds, client.getLocalCache().countNumberRegistryIds());

        // 4. Update the GeoObject
        final String newLabel = "MODIFIED DISPLAY LABEL";
        go3.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, newLabel);
        UTAH.setDisplayLabel(newLabel);
        GeoObject go4 = client.updateGeoObject(go3);
        UTAH.assertEquals(go4);

        // 5. Fetch it one last time to make sure our update worked
        GeoObject go5 = client.getGeoObjectByCode(go4.getCode(), go4.getType().getCode());
        UTAH.assertEquals(go5);
    }

    @Test
    public void testCreateGetUpdateGeoObjectOverTime() throws AuthenticationException, ServerResponseException, IOException {
        // 1. Create a Geo Object locally
        GeoObjectOverTime goUtah = UTAH.newGeoObjectOverTime();

        // 2. Send the new GeoObject to the server to be applied to the database
        GeoObjectOverTime go2 = client.createGeoObjectOverTime(goUtah);
        UTAH.setUid(go2.getUid());
        UTAH.assertEquals(go2);

        // 3. Retrieve the new GeoObject from the server
        int numRegistryIds = client.getLocalCache().countNumberRegistryIds();
        GeoObjectOverTime go3 = client.getGeoObjectOverTime(go2.getUid(), go2.getType().getCode());
        UTAH.assertEquals(go3);
        Assert.assertEquals(numRegistryIds, client.getLocalCache().countNumberRegistryIds());

        // 4. Update the GeoObject
        final String newLabel = "MODIFIED DISPLAY LABEL";
        go3.setDisplayLabel(new LocalizedValue(newLabel), new Date(), null);
        UTAH.setDisplayLabel(newLabel);
        GeoObjectOverTime go4 = client.updateGeoObjectOverTime(go3);
        UTAH.assertEquals(go4);

        // 5. Fetch it one last time to make sure our update worked
        GeoObjectOverTime go5 = client.getGeoObjectOverTimeByCode(go4.getCode(), go4.getType().getCode());
        UTAH.assertEquals(go5);
    }

    @Test
    public void testGetParentGeoObjects() throws AuthenticationException, ServerResponseException, IOException {
        int numRegistryIds = client.getLocalCache().countNumberRegistryIds();
      
        String childId = data.CO_D_TWO.getUid();
        String childTypeCode = data.CO_D_TWO.getGeoObjectType().getCode();
        String[] childrenTypes = new String[]{data.COUNTRY.getCode(), data.STATE.getCode()};

        // Recursive
        ParentTreeNode tn = client.getParentGeoObjects(childId, childTypeCode, childrenTypes, true);
        data.CO_D_TWO.assertEquals(tn, childrenTypes, true);
        Assert.assertEquals(tn.toJSON().toString(), ParentTreeNode.fromJSON(tn.toJSON().toString(), client).toJSON().toString());

        // Not recursive
        ParentTreeNode tn2 = client.getParentGeoObjects(childId, childTypeCode, childrenTypes, false);
        data.CO_D_TWO.assertEquals(tn2, childrenTypes, false);
        Assert.assertEquals(tn2.toJSON().toString(), ParentTreeNode.fromJSON(tn2.toJSON().toString(), client).toJSON().toString());

        // Test only getting countries
        String[] countryArr = new String[]{data.COUNTRY.getCode()};
        ParentTreeNode tn3 = client.getParentGeoObjects(childId, childTypeCode, countryArr, true);
        data.CO_D_TWO.assertEquals(tn3, countryArr, true);
        Assert.assertEquals(tn3.toJSON().toString(), ParentTreeNode.fromJSON(tn3.toJSON().toString(), client).toJSON().toString());
        
        Assert.assertEquals(numRegistryIds, client.getLocalCache().countNumberRegistryIds());
    }

    @Test
    public void testGetChildGeObjects() throws AuthenticationException, ServerResponseException, IOException {
        int numRegistryIds = client.getLocalCache().countNumberRegistryIds();
      
        String[] childrenTypes = new String[]{data.STATE.getCode(), data.DISTRICT.getCode()};

        // Recursive
        ChildTreeNode tn = client.getChildGeoObjects(data.USA.getUid(), data.USA.getGeoObjectType().getCode(), childrenTypes, true);
        data.USA.assertEquals(tn, childrenTypes, true);
        Assert.assertEquals(tn.toJSON().toString(), ChildTreeNode.fromJSON(tn.toJSON().toString(), client).toJSON().toString());

        // Not recursive
        ChildTreeNode tn2 = client.getChildGeoObjects(data.USA.getUid(), data.USA.getGeoObjectType().getCode(), childrenTypes, false);
        data.USA.assertEquals(tn2, childrenTypes, false);
        Assert.assertEquals(tn2.toJSON().toString(), ChildTreeNode.fromJSON(tn2.toJSON().toString(), client).toJSON().toString());

        // Test only getting districts
        String[] distArr = new String[]{data.DISTRICT.getCode()};
        ChildTreeNode tn3 = client.getChildGeoObjects(data.USA.getUid(), data.USA.getGeoObjectType().getCode(), distArr, true);
        data.USA.assertEquals(tn3, distArr, true);
        Assert.assertEquals(tn3.toJSON().toString(), ChildTreeNode.fromJSON(tn3.toJSON().toString(), client).toJSON().toString());
        
        Assert.assertEquals(numRegistryIds, client.getLocalCache().countNumberRegistryIds());
    }

    @Test
    public void testExecuteActions() throws AuthenticationException, ServerResponseException, IOException {
        // Create a new GeoObject locally
        GeoObjectOverTime goCali = CALIFORNIA.newGeoObjectOverTime();
        client.getLocalCache().createGeoObjectOverTime(goCali);

        // Update that GeoObject
        final String newLabel = "MODIFIED DISPLAY LABEL";
        goCali.setDisplayLabel(new LocalizedValue(newLabel), null, null);
        client.getLocalCache().updateGeoObjectOverTime(goCali);

        Assert.assertEquals(2, client.getLocalCache().getAllActionHistory().size());
        client.pushObjectsToRegistry();

        // TODO : This test isn't even possible anymore. Actions are not executed immediately when
        // they are received.
//        // Fetch California and make sure it has our new display label
//        GeoObject goCali2 = client.getGeoObjectByCode(CALIFORNIA.getCode(), CALIFORNIA.getUniversal().getCode());
//
//        CALIFORNIA.setUid(goCali2.getUid());
//        CALIFORNIA.setDisplayLabel(newLabel);
//        CALIFORNIA.assertEquals(goCali2);
//
//        // Update that GeoObject again
//        final String newLabel2 = "MODIFIED DISPLAY LABEL2";
//        goCali.setLocalizedDisplayLabel(newLabel2);
//        client.getLocalCache().updateGeoObject(goCali);
//
//        // Make sure that when we push it only pushes our new update and not the old ones again
//        Assert.assertEquals(1, client.getLocalCache().getUnpushedActionHistory().size());
    }

    @Test
    public void testAddChild() throws AuthenticationException, ServerResponseException, IOException {
        ParentTreeNode ptnTestState = client.addChild(data.WASHINGTON.getUid(), data.WASHINGTON.getGeoObjectType().getCode(), TEST_ADD_CHILD.getUid(), TEST_ADD_CHILD.getGeoObjectType().getCode(), data.LOCATED_IN.getCode());

        boolean found = false;
        for (ParentTreeNode ptnUSA : ptnTestState.getParents())
        {
            if (ptnUSA.getGeoObject().getCode().equals(data.WASHINGTON.getCode()))
            {
                found = true;
                break;
            }
        }
        Assert.assertTrue("Did not find our test object in the list of returned children", found);
        TEST_ADD_CHILD.assertEquals(ptnTestState.getGeoObject());

        ChildTreeNode ctnUSA2 = client.getChildGeoObjects(data.WASHINGTON.getUid(), data.WASHINGTON.getGeoObjectType().getCode(), new String[]{data.DISTRICT.getCode()}, false);

        found = false;
        for (ChildTreeNode ctnState : ctnUSA2.getChildren())
        {
            if (ctnState.getGeoObject().getCode().equals(TEST_ADD_CHILD.getCode()))
            {
                found = true;
                break;
            }
        }
        Assert.assertTrue("Did not find our test object in the list of returned children", found);
    }

    // TODO
//    @Test
//    public void testRemoveChild()
//    {
//        ParentTreeNode ptnTestState = client.addChild(data.WASHINGTON.getUid(), data.WASHINGTON.getUniversal().getCode(), TEST_ADD_CHILD.getUid(), TEST_ADD_CHILD.getUniversal().getCode(), data.LOCATED_IN.getCode());
//
//        boolean found = false;
//        for (ParentTreeNode ptnUSA : ptnTestState.getParents())
//        {
//            if (ptnUSA.getGeoObject().getCode().equals(data.WASHINGTON.getCode()))
//            {
//                found = true;
//                break;
//            }
//        }
//        Assert.assertTrue("Did not find our test object in the list of returned children", found);
//        TEST_ADD_CHILD.assertEquals(ptnTestState.getGeoObject());
//
//        ChildTreeNode ctnUSA2 = client.getChildGeoObjects(data.WASHINGTON.getUid(), data.WASHINGTON.getUniversal().getCode(), new String[]{data.DISTRICT.getCode()}, false);
//
//        found = false;
//        for (ChildTreeNode ctnState : ctnUSA2.getChildren())
//        {
//            if (ctnState.getGeoObject().getCode().equals(TEST_ADD_CHILD.getCode()))
//            {
//                found = true;
//                break;
//            }
//        }
//        Assert.assertTrue("Did not find our test object in the list of returned children", found);
//    }
}
