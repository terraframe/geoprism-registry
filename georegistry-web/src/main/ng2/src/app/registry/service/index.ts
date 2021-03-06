import { ChangeRequestService } from './change-request.service';
import { GeoObjectTypeManagementService } from './geoobjecttype-management.service';
import { HierarchyService } from './hierarchy.service';
import { IOService } from './io.service';
import { LocalizationManagerService } from './localization-manager.service';
import { MapService } from './map.service';
import { RegistryService } from './registry.service';
import { SynchronizationConfigService } from './synchronization-config.service';
import { TaskService } from './task.service';

export const services: any[] = [
	ChangeRequestService,
	GeoObjectTypeManagementService,
	HierarchyService,
	IOService,
	LocalizationManagerService,
	MapService,
	RegistryService,
	SynchronizationConfigService,
	TaskService
];

export * from './change-request.service';
export * from './geoobjecttype-management.service';
export * from './hierarchy.service';
export * from './io.service';
export * from './localization-manager.service';
export * from './map.service';
export * from './registry.service';
export * from './synchronization-config.service';
export * from './task.service';