import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { finalize } from 'rxjs/operators';
import * as mapboxgl from 'mapbox-gl';

import { LocationInformation } from '../model/location-manager';
import { EventService } from '../../shared/service/event.service';


declare var acp: any;

@Injectable()
export class MapService {

	constructor(private http: HttpClient, private eventService: EventService) {
		(mapboxgl as any).accessToken = 'pk.eyJ1IjoidGVycmFmcmFtZSIsImEiOiJjanZxNWE5bWkwazYwNGFtb3loOGxsbjR1In0.ZNEwT-pBnGookEb-BF_jQQ';
	}

	roots(typeCode: string, hierarchyCode: string, date: string): Promise<LocationInformation> {

		let params: HttpParams = new HttpParams();

		if (typeCode != null) {
			params = params.set('typeCode', typeCode);
		}

		if (hierarchyCode != null) {
			params = params.set('hierarchyCode', hierarchyCode);
		}

		if (date != null) {
			params = params.set('date', date);
		}

		this.eventService.start();

		return this.http
			.get<LocationInformation>(acp + '/registrylocation/roots', { params: params })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
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

		this.eventService.start();

		return this.http
			.get<LocationInformation>(acp + '/registrylocation/select', { params: params })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise();
	}


}