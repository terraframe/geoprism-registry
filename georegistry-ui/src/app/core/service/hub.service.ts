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

import { Injectable } from "@angular/core";
import { HttpHeaders, HttpClient, HttpParams } from "@angular/common/http";

import { Application } from "@shared/model/application";

import { environment } from 'src/environments/environment';
import { BehaviorSubject, firstValueFrom, Observable } from "rxjs";
import { MenuSection } from "@core/model/core";

@Injectable()
export class HubService {

    private expanded$: BehaviorSubject<boolean>;

    constructor(private http: HttpClient) {
        this.expanded$ = new BehaviorSubject<boolean>(false);
    }

    getExpanded(): Observable<boolean> {
        return this.expanded$;
    }

    setExpanded(expanded: boolean): void {
        this.expanded$.next(expanded);
    }

    applications(): Promise<Application[]> {
        return firstValueFrom(this.http
            .get<Application[]>(environment.apiUrl + "/api/cgr/applications"));
    }

    oauthGetPublic(id: string): Promise<any[]> {
        let params: HttpParams = new HttpParams();

        if (id) {
            params = params.set("id", id);
        }

        return firstValueFrom(this.http
            .get<any[]>(environment.apiUrl + "/api/oauth/get-public", { params: params }));
    }

    getMenuSections(): MenuSection[] {
        const sections: MenuSection[] = [];

        // The CREATE section
        sections.push({
            title: 'cgr.nav.configure.title',
            description: 'cgr.nav.configure.description',
            items: [
                {
                    id: 'HIERARCHIES',
                    description: "nav.hierarchies.description",
                    link: '/registry/hierarchies',
                    icon: 'fa-solid fa-sitemap',
                    key: 'hierarchies.menu'
                },
                {
                    id: 'CLASSIFICATION',
                    description: "nav.classifications.description",
                    link: '/registry/classification-type',
                    icon: 'fa-solid fa-bars-staggered',
                    key: 'header.classifications.option'
                },
                {
                    id: 'BUSINESS-TYPES',
                    description: "nav.business.type.description",
                    link: '/registry/business-types',
                    icon: 'fa-solid fa-briefcase',
                    key: 'business.data.type'
                }

            ]
        });

        // The CREATE section
        sections.push({
            title: 'cgr.nav.curate.title',
            description: "cgr.nav.curate.description",
            items: [
                {
                    id: 'SCHEDULED-JOBS',
                    description: "nav.scheduledjobs.description",
                    link: '/registry/scheduled-jobs',
                    icon: 'fa-solid fa-clock-rotate-left',
                    key: 'scheduledjobs.menu'
                },
                {
                    id: 'CHANGE-REQUESTS',
                    description: "nav.changerequest.description",
                    link: '/registry/change-requests',
                    icon: 'fa-solid fa-arrow-right-arrow-left',
                    key: 'header.changerequest.option'
                },
                {
                    id: 'EVENTS',
                    description: "nav.historical.events.description",
                    link: '/registry/historical-events',
                    icon: 'fa-solid fa-calendar-days',
                    key: 'historical.events'
                },
                {
                    id: 'TASKS',
                    description: "nav.tasks.description",
                    link: '/registry/tasks',
                    icon: 'fa-solid fa-angle-down',
                    key: 'header.tasks'
                },
                {
                    id: 'IMPORT',
                    description: "nav.importer.description",
                    link: '/registry/data',
                    icon: 'fa-solid fa-cloud-arrow-up',
                    key: 'header.data.option'
                },
                {
                    id: 'BUSINESS-TYPES',
                    description: "nav.importer.business.description",
                    link: '/registry/business-importer',
                    icon: 'fa-solid fa-file-import',
                    key: 'business.data.import'
                },
                {
                    id: 'BUSINESS-TYPES',
                    description: "nav.importer.edge.description",
                    link: '/registry/edge-importer',
                    icon: 'fa-solid fa-file-arrow-up',
                    key: 'edge.data.import'
                },

            ]
        });

        // The EXPLORE section
        sections.push({
            title: 'cgr.nav.explore.title',
            description: 'cgr.nav.explore.description',
            items: [
                {
                    id: 'NAVIGATOR',
                    description: "nav.navigator.description",
                    link: '/registry/location-manager',
                    icon: 'fa-regular fa-map',
                    key: 'navigator.menu'
                },
                {
                    id: 'LISTS',
                    description: "nav.list.description",
                    link: '/registry/master-lists',
                    icon: 'fa-solid fa-table-list',
                    key: 'masterlist.menu'
                },
                {
                    id: 'EXPORT',
                    description: "nav.export.description",
                    link: '/registry/export',
                    icon: 'fa-solid fa-file-export',
                    key: 'header.export'
                },
                {
                    id: 'LPG',
                    description: "nav.lpg.description",
                    link: '/registry/labeled-property-graph-type',
                    icon: 'fa-solid fa-hexagon-nodes',
                    key: 'lpg.menu'
                },

            ]
        });


        return sections;
    }
}
