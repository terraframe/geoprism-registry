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
import { HttpClient, HttpParams } from "@angular/common/http";
// import 'rxjs/add/operator/toPromise';
import { finalize } from "rxjs/operators";

import { EventService } from "@shared/service";

import { ActivatedRoute } from "@angular/router";
import { ObjectReference, Relationship, TreeData } from "@registry/model/graph";
import { environment } from 'src/environments/environment';

@Injectable({ providedIn: 'root' })
export class RelationshipVisualizationService {

    constructor(private http: HttpClient, private eventService: EventService, private route: ActivatedRoute) {
    }

    tree(relationshipType: string, graphTypeCode: string, sourceVertex: ObjectReference, date: string, boundsWKT: string, nullDateIsLatest: boolean = true): Promise<TreeData> {
        let params: HttpParams = new HttpParams();
        params = params.set("sourceVertex", JSON.stringify(sourceVertex));
        params = params.set("nullDateIsLatest", nullDateIsLatest);

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
            .get<TreeData>(environment.apiUrl + "/api/relationship-visualization/tree", { params: params })
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
            .get<any>(environment.apiUrl + "/api/relationship-visualization/treeAsGeoJson", { params: params })
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
            .get<any>(environment.apiUrl + "/api/relationship-visualization/relationships", { params: params })
            .pipe(finalize(() => {
                // this.eventService.complete();
            }))
            .toPromise();
    }

    deleteEdge(relationshipType: string, graphTypeCode: string, oid: string): Promise<void> {
        let params: HttpParams = new HttpParams();
        params = params.set("oid", oid);
        params = params.set("relationshipType", relationshipType);
        params = params.set("graphTypeCode", graphTypeCode);

        return this.http
            .get<void>(
                environment.apiUrl + "/api/relationship-visualization/deleteEdge",
                { params: params }
            )
            .pipe(finalize(() => {
                // this.eventService.complete();
            }))
            .toPromise();
    }

    relationshipCounts(
        objectType: "BUSINESS" | "GEOOBJECT",
        typeCode: string,
        sourceVertex: ObjectReference
    ): Promise<{relationships: Relationship[], stabilityPeriods: any}> {
        let params: HttpParams = new HttpParams();

        params = params.set("objectType", objectType);
        params = params.set("typeCode", typeCode);
        params = params.set("sourceVertex", JSON.stringify(sourceVertex));

        return this.http
            .get<any>(
                environment.apiUrl
                    + "/api/relationship-visualization/relationship-counts",
                { params: params }
            )
            .pipe(finalize(() => {
                // this.eventService.complete();
            }))
            .toPromise();
    }

}
