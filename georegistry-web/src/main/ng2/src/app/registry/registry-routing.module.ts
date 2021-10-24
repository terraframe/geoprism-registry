import { NgModule } from "@angular/core";
import { LocationStrategy, HashLocationStrategy } from "@angular/common";
import { Routes, RouterModule } from "@angular/router";

import { HierarchyComponent } from "./component/hierarchy/hierarchy.component";
import { SubmitChangeRequestComponent } from "./component/submit-change-request/submit-change-request.component";
import { MasterListManagerComponent } from "./component/master-list/master-list-manager.component";
import { MasterListComponent } from "./component/master-list/master-list.component";
import { MasterListViewComponent } from "./component/master-list/master-list-view.component";
import { DataPageComponent } from "./component/data-page/data-page.component";
import { ChangeRequestPageComponent } from "./component/change-request-page/change-request-page.component";
import { ScheduledJobsComponent } from "./component/scheduled-jobs/scheduled-jobs.component";
import { JobComponent } from "./component/scheduled-jobs/job.component";
import { TaskViewerComponent } from "./component/task-viewer/task-viewer.component";
import { SynchronizationConfigManagerComponent } from "./component/synchronization-config/synchronization-config-manager.component";
import { SynchronizationConfigComponent } from "./component/synchronization-config/synchronization-config.component";
import { SyncDetailsComponent } from "./component/synchronization-config/details.component";
import { LocationManagerComponent } from "./component/location-manager/location-manager.component";
import { DatasetLocationManagerComponent } from "./component/location-manager/dataset-location-manager.component";

import { MaintainerGuard, ContributerGuard, AuthGuard } from "../shared/service/guard.service";
import { TransitionEventTableComponent } from "./component/transition-event/transition-event-table.component";
import { BusinessTypeManagerComponent } from "./component/business-type/business-type-manager.component";
import { BusinessImporterComponent } from "./component/business-importer/business-importer.component";

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
        component: MasterListManagerComponent,
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
        path: "master-list/:oid/:published",
        component: MasterListComponent,
        canActivate: [AuthGuard]
    },
    {
        path: "master-list-view/:oid",
        component: MasterListViewComponent,
        canActivate: [AuthGuard]
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
        path: "location-manager/:geoobjectuid/:geoobjecttypecode/:datestr/:hideSearchOptions",
        component: LocationManagerComponent,
        canActivate: [AuthGuard]
    },
    {
        path: "location-manager/:geoobjectuid/:geoobjecttypecode/:datestr/:hideSearchOptions/:backReference",
        component: LocationManagerComponent,
        canActivate: [AuthGuard]
    },
    {
        path: "dataset-location-manager/:datasetId/:typeCode/:readOnly/:date",
        component: DatasetLocationManagerComponent,
        canActivate: [AuthGuard]
    },
    {
        path: "dataset-location-manager/:datasetId/:typeCode/:readOnly/:date/:code",
        component: DatasetLocationManagerComponent,
        canActivate: [AuthGuard]
    },
    {
        path: "dataset-location-manager/:datasetId/:typeCode/:readOnly/:date/:code/:editOnly",
        component: DatasetLocationManagerComponent,
        canActivate: [AuthGuard]
    },
    {
        path: "dataset-location-manager/:datasetId/:typeCode/:readOnly/:date/:code/:editOnly/:backReference",
        component: DatasetLocationManagerComponent,
        canActivate: [AuthGuard]
    },
    {
        path: 'sync/details/:config/:oid',
        component: SyncDetailsComponent,
        canActivate: [MaintainerGuard]
    },
    {
        path: "transition-events",
        component: TransitionEventTableComponent,
        canActivate: [MaintainerGuard]
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
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule],
    providers: [
        { provide: LocationStrategy, useClass: HashLocationStrategy }
    ]
})
export class RegistryRoutingModule { }
