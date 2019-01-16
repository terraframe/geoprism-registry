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

import { Injectable, Output, EventEmitter } from '@angular/core';
// import { Headers, Http, Response, URLSearchParams } from '@angular/http';
// import 'rxjs/add/operator/map';
// import 'rxjs/add/operator/debounceTime';
// import 'rxjs/add/operator/distinctUntilChanged';
// import 'rxjs/add/operator/switchMap';
// import 'rxjs/add/operator/toPromise';
// import 'rxjs/add/operator/finally';

import { Observable } from 'rxjs/Observable';
import { Subject }    from 'rxjs';

// import { TreeEntity, HierarchyType, Hierarchy, HierarchyNode, GeoObject, GeoObjectType, Attribute, Term } from '../data/hierarchy/hierarchy';
// import { EventService } from '../event/event.service';

declare var acp: any;

// import { TreeNode } from 'angular-tree-component';


@Injectable()
export class GeoObjTypeModalService {

    constructor( ) { }

    state: string  = "";
    private modalStateSource = new Subject<string>();

    modalState = this.modalStateSource.asObservable();


    setState(state: string) {
        this.state = state;
        this.modalStateSource.next(this.state);
    }

}
