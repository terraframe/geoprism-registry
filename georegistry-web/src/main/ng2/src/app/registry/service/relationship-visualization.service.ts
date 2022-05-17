///
/// Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Runway SDK(tm).
///
/// Runway SDK(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Runway SDK(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Runway SDK(tm).  If not, see <ehttp://www.gnu.org/licenses/>.
///

import { Injectable } from "@angular/core";
import { HttpClient, HttpParams } from "@angular/common/http";
// import 'rxjs/add/operator/toPromise';
import { finalize } from "rxjs/operators";

import { EventService } from "@shared/service";

import { GeoRegistryConfiguration } from "@core/model/registry";

import { LocalizedValue } from "@shared/model/core";
import { ActivatedRoute } from "@angular/router";
import { ObjectReference, Relationship, TreeData } from "@registry/model/graph";
declare let registry: GeoRegistryConfiguration;

@Injectable()
export class RelationshipVisualizationService {

    constructor(private http: HttpClient, private eventService: EventService, private route: ActivatedRoute) {
    }

    tree(relationshipType: string, graphTypeCode: string, sourceVertex: ObjectReference, date: string, boundsWKT: string): Promise<TreeData> {
        let params: HttpParams = new HttpParams();
        params = params.set("sourceVertex", JSON.stringify(sourceVertex));

        if (relationshipType != null) {
            params = params.set("relationshipType", relationshipType);
        }

        if (graphTypeCode != null) {
            params = params.set("graphTypeCode", graphTypeCode);
        }

        if (date) {
            params = params.set("date", date);
        }

        if (boundsWKT) {
            params = params.set("boundsWKT", boundsWKT);
        }

        // this.eventService.start();

        return this.http
            .get<TreeData>(registry.contextPath + "/relationship-visualization/tree", { params: params })
            .pipe(finalize(() => {
                // this.eventService.complete();
            }))
            .toPromise();
    }

    treeAsGeoJson(relationshipType: string, graphTypeCode: string, sourceObject: ObjectReference, date: string, boundsWKT: string): Promise<any> {
        let params: HttpParams = new HttpParams();
        params = params.set("sourceVertex", JSON.stringify(sourceObject));
        params = params.set("graphTypeCode", graphTypeCode);

        if (relationshipType != null) {
            params = params.set("relationshipType", relationshipType);
        }

        if (date) {
            params = params.set("date", date);
        }

        if (boundsWKT) {
            params = params.set("boundsWKT", boundsWKT);
        }

        // this.eventService.start();

        return this.http
            .get<any>(registry.contextPath + "/relationship-visualization/treeAsGeoJson", { params: params })
            .pipe(finalize(() => {
                // this.eventService.complete();
            }))
            .toPromise();
    }

    relationships(objectType: "BUSINESS" | "GEOOBJECT", typeCode: string): Promise<Relationship[]> {
        let params: HttpParams = new HttpParams();
        params = params.set("objectType", objectType);
        params = params.set("typeCode", typeCode);

        // this.eventService.start();

        return this.http
            .get<any>(registry.contextPath + "/relationship-visualization/relationships", { params: params })
            .pipe(finalize(() => {
                // this.eventService.complete();
            }))
            .toPromise();
    }

}
