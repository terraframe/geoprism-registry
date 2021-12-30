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
import { HistoricalEventModuleComponent } from "./component/historical-event-module/historical-event-module.component";
import { ListTypeManagerComponent } from "./component/list-type/list-type-manager.component";
import { ListComponent } from "./component/list-type/list.component";
import { CurationJobComponent } from "./component/curation/curation-job.component";

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
        path: 'sync/details/:config/:oid',
        component: SyncDetailsComponent,
        canActivate: [MaintainerGuard]
    },
    {
        path: 'curation-job/:oid',
        component: CurationJobComponent,
        canActivate: [MaintainerGuard]
    },
    {
        path: "historical-events",
        component: HistoricalEventModuleComponent,
        canActivate: [ContributerGuard]
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
