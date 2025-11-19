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
            title: 'CREATE',
            description: 'Create a framework',
            items: [
                {
                    id: 'HIERARCHIES',
                    description: "Start with your organization's data hierarchy",
                    link: '/registry/hierarchies',
                    icon: 'fa-solid fa-sitemap',
                    key: 'hierarchies.menu'
                },
                {
                    id: 'CLASSIFICATION',
                    description: "Define ontology classifications",
                    link: '/registry/classification-type',
                    icon: 'fa-solid fa-bars-staggered',
                    key: 'header.classifications.option'
                },
                {
                    id: 'BUSINESS-TYPES',
                    description: "Create business type definitions",
                    link: '/registry/business-types',
                    icon: 'fa-solid fa-briefcase',
                    key: 'business.data.type'
                }

            ]
        });

        // The CREATE section
        sections.push({
            title: 'CURATE',
            description: "Edit the details",
            items: [
                {
                    id: 'SCHEDULED-JOBS',
                    description: "View scheduled jobs",
                    link: '/registry/scheduled-jobs',
                    icon: 'fa-solid fa-clock-rotate-left',
                    key: 'scheduledjobs.menu'
                },
                {
                    id: 'CHANGE-REQUESTS',
                    description: "Manage change requests",
                    link: '/registry/change-requests',
                    icon: 'fa-solid fa-arrow-right-arrow-left',
                    key: 'header.changerequest.option'
                },
                {
                    id: 'EVENTS',
                    description: "Manage historical events",
                    link: '/registry/historical-events',
                    icon: 'fa-solid fa-calendar-days',
                    key: 'historical.events'
                },
                {
                    id: 'TASKS',
                    description: "View curation tasks",
                    link: '/registry/tasks',
                    icon: 'fa-solid fa-angle-down',
                    key: 'header.tasks'
                },
                {
                    id: 'IMPORT',
                    description: "Import GeoObject data",
                    link: '/registry/data',
                    icon: 'fa-solid fa-cloud-arrow-up',
                    key: 'header.data.option'
                },
                {
                    id: 'BUSINESS-TYPES',
                    description: "Import Business data",
                    link: '/registry/business-importer',
                    icon: 'fa-solid fa-file-import',
                    key: 'business.data.import'
                },
                {
                    id: 'BUSINESS-TYPES',
                    description: "Import edges between business objects",
                    link: '/registry/edge-importer',
                    icon: 'fa-solid fa-file-arrow-up',
                    key: 'edge.data.import'
                },

            ]
        });

        // The EXPLORE section
        sections.push({
            title: 'EXPLORE',
            description: 'Share the data',
            items: [
                {
                    id: 'NAVIGATOR',
                    description: "Explore the data on a map",
                    link: '/registry/location-manager',
                    icon: 'fa-regular fa-map',
                    key: 'navigator.menu'
                },
                {
                    id: 'LISTS',
                    description: "Manage lists of the data",
                    link: '/registry/master-lists',
                    icon: 'fa-solid fa-table-list',
                    key: 'masterlist.menu'
                },
                {
                    id: 'EXPORT',
                    description: "Export data to RDF",
                    link: '/registry/export',
                    icon: 'fa-solid fa-file-export',
                    key: 'header.export'
                },
                {
                    id: 'LPG',
                    description: "Manage Label Property Graphs of the data",
                    link: '/registry/labeled-property-graph-type',
                    icon: 'fa-solid fa-hexagon-nodes',
                    key: 'lpg.menu'
                },

            ]
        });


        return sections;
    }
}
