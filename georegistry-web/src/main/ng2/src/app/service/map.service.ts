import { Injectable } from '@angular/core';
import { Headers, Http, Response, URLSearchParams, RequestOptions, ResponseContentType } from '@angular/http';
import { Observable } from 'rxjs';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/toPromise';
import 'rxjs/add/operator/finally';

import { GeoJSONSource } from 'mapbox-gl';

import * as mapboxgl from 'mapbox-gl';

declare var acp: any;

@Injectable()
export class MapService {

    constructor( private http: Http ) {
        ( mapboxgl as any ).accessToken = 'pk.eyJ1IjoidGVycmFmcmFtZSIsImEiOiJjanZxNTFnaTYyZ2RuNDlxcmNnejNtNjN6In0.-kmlS8Tgb2fNc1NPb5rJEQ';
    }

    features(): Promise<{ features: GeoJSONSource, bbox: number[] }> {
        let params: URLSearchParams = new URLSearchParams();

        return this.http
            .get( acp + '/project/features', { search: params } )
            .toPromise()
            .then( response => {
                return response.json() as { features: GeoJSONSource, bbox: number[] };
            } )
    }


}