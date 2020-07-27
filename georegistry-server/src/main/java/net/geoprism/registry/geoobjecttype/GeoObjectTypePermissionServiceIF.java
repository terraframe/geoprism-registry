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
package net.geoprism.registry.geoobjecttype;

import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.business.rbac.SingleActorDAOIF;

public interface GeoObjectTypePermissionServiceIF
{

  public boolean canRead(SingleActorDAOIF actor, String orgCode);

  public void enforceCanRead(SingleActorDAOIF actor, String orgCode, String gotLabel);

  public boolean canWrite(SingleActorDAOIF actor, String orgCode);

  public void enforceCanWrite(SingleActorDAOIF actor, String orgCode, String gotLabel);

  public boolean canCreate(SingleActorDAOIF actor, String orgCode);

  public void enforceCanCreate(SingleActorDAOIF actor, String orgCode);

  public boolean canDelete(SingleActorDAOIF actor, String orgCode);

  public void enforceCanDelete(SingleActorDAOIF actor, String orgCode, String gotLabel);

  public void enforceActorHasPermission(SingleActorDAOIF actor, String orgCode, String gotLabel, Operation op);

}
