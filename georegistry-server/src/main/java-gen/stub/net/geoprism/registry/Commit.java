/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry;

import net.geoprism.registry.model.SnapshotContainer;
import net.geoprism.registry.view.CommitDTO;

public class Commit extends CommitBase implements SnapshotContainer<CommitHasSnapshot>
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 372845489;

  public Commit()
  {
    super();
  }

  @Override
  public boolean createTablesWithSnapshot()
  {
    return false;
  }

  public CommitDTO toDTO(Publish publish)
  {
    return new CommitDTO(this.getUid(), publish.getUid(), this.getVersionNumber(), getLastOriginGlobalIndex(), this.getCreateDate());
  }

  public CommitDTO toDTO()
  {
    return this.toDTO(this.getPublish());
  }
}
