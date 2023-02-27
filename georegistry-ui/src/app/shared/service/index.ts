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

import { AuthService } from "./auth.service";
import { DateService } from "./date.service";
import { EventService } from "./event.service";
import { ExternalSystemService } from "./external-system.service";
import { AdminGuard, AuthGuard, ContributerGuard, MaintainerGuard } from "./guard.service";
import { LocalizationService } from "./localization.service";
import { ModalStepIndicatorService } from "./modal-step-indicator.service";
import { OrganizationService } from "./organization.service";
import { PendingChangesGuard } from "./pending-changes-guard";
import { ProfileService } from "./profile.service";
import { ProgressService } from "./progress.service";
import { SessionService } from "./session.service";

export const services: any[] = [
    AuthService,
    EventService,
    ExternalSystemService,
    AdminGuard,
    AuthGuard,
    ContributerGuard,
    MaintainerGuard,
    ModalStepIndicatorService,
    OrganizationService,
    PendingChangesGuard,
    ProfileService,
    ProgressService,
    SessionService,
    DateService,
    LocalizationService
];

export * from "./auth.service";
export * from "./event.service";
export * from "./external-system.service";
export * from "./guard.service";
export * from "./localization.service";
export * from "./modal-step-indicator.service";
export * from "./organization.service";
export * from "./pending-changes-guard";
export * from "./profile.service";
export * from "./progress.service";
export * from "./session.service";
export * from "./date.service";
export * from "./localization.service";
