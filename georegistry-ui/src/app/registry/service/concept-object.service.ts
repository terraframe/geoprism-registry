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

import { EventService } from "@shared/service";
import { ObjectService } from "./object.service";
import { ObjectOverTime, ObjectOverTimeNode } from "@registry/model/object-class";
import { environment } from "src/environments/environment";
import { finalize, firstValueFrom } from "rxjs";
import { AttributedType, AttributeType } from "@registry/model/registry";
import { PageResult } from "@shared/model/core";

@Injectable({ providedIn: 'root' })
export class ConceptObjectService extends ObjectService {

    // eslint-disable-next-line no-useless-constructor
    constructor(protected http: HttpClient, protected eventService: EventService) {
        super(http, eventService);
    }

    controller() {
        return "/api/concept-object";
    }

    search(type: AttributedType, attribute: AttributeType, text: string): Promise<ObjectOverTime[]> {
        let params: HttpParams = new HttpParams();
        params = params.append("typeCode", type.code);
        params = params.append("attribute", attribute.code);
        params = params.append("text", text);

        return firstValueFrom(this.http.get<ObjectOverTime[]>(environment.apiUrl + this.controller() + "/search", { params: params }));
    }

    getChildren(type: AttributedType, attribute: AttributeType, concept: string, pageNumber: number, pageSize: number): Promise<PageResult<ObjectOverTime>> {
        let params: HttpParams = new HttpParams();
        params = params.append("typeCode", type.code);
        params = params.append("attribute", attribute.code);
        params = params.append("concept", concept);
        params = params.append("pageNumber", pageNumber);
        params = params.append("pageSize", pageSize);

        return firstValueFrom(this.http.get<PageResult<ObjectOverTime>>(environment.apiUrl + this.controller() + "/get-children", { params: params }));
    }

    getAncestorTree(type: AttributedType, attribute: AttributeType, concept: string, pageSize: number): Promise<ObjectOverTimeNode> {
        let params: HttpParams = new HttpParams();
        params = params.append("typeCode", type.code);
        params = params.append("attribute", attribute.code);
        params = params.append("concept", concept);
        params = params.append("pageSize", pageSize);

        return firstValueFrom(this.http.get<ObjectOverTimeNode>(environment.apiUrl + this.controller() + "/get-ancestor-tree", { params: params }));
    }    
}
