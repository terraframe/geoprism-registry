import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/toPromise';
import 'rxjs/add/operator/finally';

import { GeoJSONSource } from 'mapbox-gl';

import * as mapboxgl from 'mapbox-gl';

declare var acp: any;

@Injectable()
export class MapService {

    constructor( private http: HttpClient ) {
        ( mapboxgl as any ).accessToken = 'pk.eyJ1IjoidGVycmFmcmFtZSIsImEiOiJjanZxNTFnaTYyZ2RuNDlxcmNnejNtNjN6In0.-kmlS8Tgb2fNc1NPb5rJEQ';
    }

    features(): Promise<{ features: GeoJSONSource, bbox: number[] }> {
        let params: HttpParams = new HttpParams();

        return this.http
            .get<{ features: GeoJSONSource, bbox: number[] }>( acp + '/project/features', { params: params } )
            .toPromise();
    }


}