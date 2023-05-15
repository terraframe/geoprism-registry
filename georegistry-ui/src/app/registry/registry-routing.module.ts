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

import { NgModule } from "@angular/core";
import { LocationStrategy, HashLocationStrategy } from "@angular/common";
import { Routes, RouterModule } from "@angular/router";

import { HierarchyComponent } from "./component/hierarchy/hierarchy.component";
import { SubmitChangeRequestComponent } from "./component/submit-change-request/submit-change-request.component";
import { DataPageComponent } from "./component/data-page/data-page.component";
import { ChangeRequestPageComponent } from "./component/change-request-page/change-request-page.component";
import { ScheduledJobsComponent } from "./component/scheduled-jobs/scheduled-jobs.component";
import { JobComponent } from "./component/scheduled-jobs/job.component";
import { TaskViewerComponent } from "./component/task-viewer/task-viewer.component";
import { SynchronizationConfigManagerComponent } from "./component/synchronization-config/synchronization-config-manager.component";
import { SynchronizationConfigComponent } from "./component/synchronization-config/synchronization-config.component";
import { SyncDetailsComponent } from "./component/synchronization-config/details.component";
import { LocationManagerComponent } from "./component/location-manager/location-manager.component";

import { MaintainerGuard, ContributerGuard, AuthGuard } from "../shared/service/guard.service";
import { BusinessTypeManagerComponent } from "./component/business-type/business-type-manager.component";
import { BusinessImporterComponent } from "./component/business-importer/business-importer.component";
import { HistoricalEventModuleComponent } from "./component/historical-event-module/historical-event-module.component";
import { ListTypeManagerComponent } from "./component/list-type/list-type-manager.component";
import { ListComponent } from "./component/list-type/list.component";
import { CurationJobComponent } from "./component/curation/curation-job.component";
import { BusinessTableComponent } from "./component/business-table/business-table.component";
import { ClassificationTypeManagerComponent } from "./component/classification-type/classification-type-manager.component";
import { LabeledPropertyGraphTypeManagerComponent } from "./component/labeled-property-graph-type/labeled-property-graph-type-manager.component";

const routes: Routes = [
    {
        path: "",
        component: HierarchyComponent,
        canActivate: [AuthGuard]
    },
    {
        path: "hierarchies",
        component: HierarchyComponent,
        canActivate: [AuthGuard]
    },
    {
        path: "data",
        component: DataPageComponent,
        canActivate: [MaintainerGuard]
    },
    {
        path: "change-requests",
        component: ChangeRequestPageComponent,
        canActivate: [ContributerGuard]
    },
    {
        path: "change-requests/:oid",
        component: ChangeRequestPageComponent,
        canActivate: [ContributerGuard]
    },
    {
        path: "master-lists",
        component: ListTypeManagerComponent,
        canActivate: [AuthGuard]
    },
    {
        path: "master-list/:oid",
        component: ListComponent,
        canActivate: [AuthGuard]
    },
    {
        path: "scheduled-jobs",
        component: ScheduledJobsComponent,
        canActivate: [MaintainerGuard]
    },
    {
        path: "job/:oid",
        component: JobComponent,
        canActivate: [MaintainerGuard]
    },
    {
        path: "change-request",
        component: SubmitChangeRequestComponent,
        canActivate: [ContributerGuard]
    },
    {
        path: "tasks",
        component: TaskViewerComponent,
        canActivate: [MaintainerGuard]
    },
    {
        path: "synchronization-configs",
        component: SynchronizationConfigManagerComponent,
        canActivate: [AuthGuard]
    },
    {
        path: "synchronization-config/:oid",
        component: SynchronizationConfigComponent,
        canActivate: [AuthGuard]
    },
    {
        path: "location-manager",
        component: LocationManagerComponent,
        canActivate: [AuthGuard]
    },
    {
        path: "sync/details/:config/:oid",
        component: SyncDetailsComponent,
        canActivate: [MaintainerGuard]
    },
    {
        path: "curation-job/:oid",
        component: CurationJobComponent,
        canActivate: [MaintainerGuard]
    },
    {
        path: "historical-events",
        component: HistoricalEventModuleComponent,
        canActivate: [ContributerGuard]
    },
    {
        path: "business-types",
        component: BusinessTypeManagerComponent,
        canActivate: [AuthGuard]
    },
    {
        path: "business-importer",
        component: BusinessImporterComponent,
        canActivate: [AuthGuard]
    },
    {
        path: "business-type/:oid",
        component: BusinessTableComponent,
        canActivate: [AuthGuard]
    },
    {
        path: "classification-type",
        component: ClassificationTypeManagerComponent,
        canActivate: [AuthGuard]
    },
    {
        path: "labeled-property-graph-type",
        component: LabeledPropertyGraphTypeManagerComponent,
        canActivate: [AuthGuard]
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule],
    providers: [
        { provide: LocationStrategy, useClass: HashLocationStrategy }
    ]
})
export class RegistryRoutingModule { }
