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

import { Injectable } from '@angular/core';
import { Headers, Http, Response, URLSearchParams } from '@angular/http';
import 'rxjs/add/operator/toPromise';
import 'rxjs/add/operator/finally';

//import { SiteEntity } from './management';
import { EventService } from '../event/event.service';

declare var acp: any;


//import { EventService, BasicService } from '../core/service/core.service';
//import { EventHttpService } from '../../core/service/event-http.service';

//import { Category, BasicCategory } from '../model/category';

declare var acp: any;

//@Injectable()
//export class HierarchyService extends BasicService {
//  
//  constructor(service: EventService, private ehttp: EventHttpService, private http: Http) {
//    super(service); 
//  }
//  
//  
//}

@Injectable()
export class HierarchyService {

    constructor( private http: Http, private eventService: EventService ) { }

//    getChildren( id: string ): Promise<SiteEntity[]> {
//        let params: URLSearchParams = new URLSearchParams();
//        params.set( 'id', id );
//
//
//        return this.http
//            .get( acp + '/project/get-children', { search: params } )
//            .toPromise()
//            .then( response => {
//                return response.json() as SiteEntity[];
//            } )
//    }



//    applyWithParent( entity: SiteEntity, parentId: string ): Promise<SiteEntity> {
//
//        let headers = new Headers( {
//            'Content-Type': 'application/json'
//        } );
//
//        this.eventService.start();
//
//        return this.http
//            .post( acp + '/project/apply-with-parent', JSON.stringify( { entity: entity, parentId: parentId } ), { headers: headers } )
//            .finally(() => {
//                this.eventService.complete();
//            } )
//            .toPromise()
//            .then( response => {
//                return response.json() as SiteEntity;
//            } )
//    }

}
