///
/// Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Geoprism Registry(tm).
///
/// Geoprism Registry(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Geoprism Registry(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
///

import { ChangeRequestService } from './change-request.service';
import { HierarchyService } from './hierarchy.service';
import { IOService } from './io.service';
import { LocalizationManagerService } from './localization-manager.service';
import { MapService } from './map.service';
import { RegistryService } from './registry.service';
import { SynchronizationConfigService } from './synchronization-config.service';
import { TaskService } from './task.service';
import { GeometryService} from './geometry.service';

export const services: any[] = [
	ChangeRequestService,
	HierarchyService,
	IOService,
	LocalizationManagerService,
	MapService,
	RegistryService,
	SynchronizationConfigService,
	TaskService,
	GeometryService
];

export * from './change-request.service';
export * from './hierarchy.service';
export * from './io.service';
export * from './localization-manager.service';
export * from './map.service';
export * from './registry.service';
export * from './synchronization-config.service';
export * from './task.service';
export * from './geometry.service';