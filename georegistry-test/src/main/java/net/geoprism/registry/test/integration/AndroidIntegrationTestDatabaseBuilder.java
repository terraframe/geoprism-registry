/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.test.integration;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;

import net.geoprism.registry.test.USATestData;

/**
 * This class must be run before running the Andorid FullStackIntegrationTest. This class brings your database
 * to a state which will enable the test to run successfully, regardless of any tests that ran before it.
 * 
 * @author rrowlands
 */
public class AndroidIntegrationTestDatabaseBuilder
{
  public static void main(String[] args)
  {
    AndroidIntegrationTestDatabaseBuilder.buildFromMain();
  }
  
  @Request
  private static void buildFromMain()
  {
    USATestData data = USATestData.newTestData();
    
    build(data);
  }
  
  @Request
  public static void build(USATestData data)
  {
    buildInTransaction(data);
  }
  
  @Transaction
  private static void buildInTransaction(USATestData data)
  {
    data.newTestGeoObjectInfo("Utah", data.STATE).delete();
    data.newTestGeoObjectInfo("California", data.STATE).delete();
    data.newTestGeoObjectInfo("TEST_ADD_CHILD", data.DISTRICT).apply(null);
  }
}
