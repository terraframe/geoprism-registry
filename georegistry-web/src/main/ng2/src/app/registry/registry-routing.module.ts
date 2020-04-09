import { NgModule } from '@angular/core';
import { LocationStrategy, HashLocationStrategy } from '@angular/common';
import { Routes, RouterModule } from '@angular/router';

import { HierarchyComponent } from './component/hierarchy/hierarchy.component';
import { SubmitChangeRequestComponent } from './component/submit-change-request/submit-change-request.component';
import { MasterListManagerComponent } from './component/master-list/master-list-manager.component';
import { MasterListComponent } from './component/master-list/master-list.component';
import { MasterListViewComponent } from './component/master-list/master-list-view.component';
import { DataPageComponent } from './component/data-page/data-page.component';
import { ChangeRequestPageComponent } from './component/change-request-page/change-request-page.component';
import { ScheduledJobsComponent } from './component/scheduled-jobs/scheduled-jobs.component';
import { JobComponent } from './component/scheduled-jobs/job.component';
import { TaskViewerComponent } from './component/task-viewer/task-viewer.component';

import { MaintainerGuard, ContributerGuard } from '../shared/service/guard.service';

const routes: Routes = [
    {
        path: '',
        component: HierarchyComponent,
        canActivate: [MaintainerGuard]
    },
    {
        path: 'hierarchies',
        component: HierarchyComponent,
        canActivate: [MaintainerGuard]
    },
    {
        path: 'data',
        component: DataPageComponent,
        canActivate: [MaintainerGuard]
    },
    {
        path: 'change-requests',
        component: ChangeRequestPageComponent,
        canActivate: [ContributerGuard]
    },
    {
        path: 'master-lists',
        component: MasterListManagerComponent,
        canActivate: [ContributerGuard]
    },
    {
        path: 'scheduled-jobs',
        component: ScheduledJobsComponent,
        canActivate: [ContributerGuard]
    },
    {
        path: 'job/:oid',
        component: JobComponent,
        canActivate: [ContributerGuard]
    },
    {
        path: 'master-list/:oid/:published',
        component: MasterListComponent,
        canActivate: [ContributerGuard]
    },
    {
        path: 'master-list-view/:oid',
        component: MasterListViewComponent,
        canActivate: [ContributerGuard]
    },
    {
        path: 'change-request',
        component: SubmitChangeRequestComponent,
        canActivate: [ContributerGuard]
    },
    {
        path: 'tasks',
        component: TaskViewerComponent,
        canActivate: [ContributerGuard]
    }
];

@NgModule( {
    imports: [RouterModule.forChild( routes )],
    exports: [RouterModule],
    providers: [
        { provide: LocationStrategy, useClass: HashLocationStrategy },
    ]
} )
export class RegistryRoutingModule { }