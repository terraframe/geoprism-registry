import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';

import { LocationInformation } from '../model/location-manager';

import * as mapboxgl from 'mapbox-gl';

declare var acp: any;

@Injectable()
export class MapService {

	constructor(private http: HttpClient) {
		(mapboxgl as any).accessToken = 'pk.eyJ1IjoidGVycmFmcmFtZSIsImEiOiJjanZxNWE5bWkwazYwNGFtb3loOGxsbjR1In0.ZNEwT-pBnGookEb-BF_jQQ';
	}

	roots(typeCode: string, hierarchyCode: string, date: string): Promise<LocationInformation> {
		let params: HttpParams = new HttpParams();

		if (typeCode != null) {
			params = params.set('typeCode', typeCode);
		}

		if (hierarchyCode != null) {
			params = params.set('hierarchyCode', typeCode);
		}

		return this.http
			.get<LocationInformation>(acp + '/registrylocation/roots', { params: params })
			.toPromise();
	}

	select(code: string, typeCode: string, childTypeCode: string, hierarchyCode: string, date: string): Promise<LocationInformation> {
		let params: HttpParams = new HttpParams();
		params = params.set('code', code);
		params = params.set('typeCode', typeCode);

		if (date != null) {
			params = params.set('date', date);
		}

		if (childTypeCode != null) {
			params = params.set('childTypeCode', childTypeCode);
		}

		if (hierarchyCode != null) {
			params = params.set('hierarchyCode', hierarchyCode);
		}

		return this.http
			.get<LocationInformation>(acp + '/registrylocation/select', { params: params })
			.toPromise();
	}


}