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
import { HttpClient } from "@angular/common/http";

import { EventService } from "@shared/service";

import { BusinessEdgeType } from "@registry/model/object-class";
import { EdgeClassService } from "./edge-class.service";

@Injectable({ providedIn: 'root' })
export class BusinessEdgeTypeService extends EdgeClassService<BusinessEdgeType> {

    constructor(http: HttpClient, eventService: EventService) {
        super(http, eventService);
    }

    controller(): string {
        return '/api/business-edge-type'
    }
}
