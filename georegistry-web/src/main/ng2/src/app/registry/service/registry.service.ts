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
import { HttpHeaders, HttpClient, HttpParams } from '@angular/common/http';

import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

import {
	GeoObject, GeoObjectType, Attribute, Term, MasterList, MasterListVersion, ParentTreeNode,
	ChildTreeNode, ValueOverTime, GeoObjectOverTime, HierarchyOverTime, ScheduledJob, PaginationPage, MasterListByOrg
} from '../model/registry';
import { HierarchyType } from '../model/hierarchy';
import { Progress } from '../../shared/model/progress';
import { EventService } from '../../shared/service/event.service';
import { Organization } from '../../shared/model/core';

declare var acp: any;


@Injectable()
export class RegistryService {

	constructor(private http: HttpClient, private eventService: EventService) { }

	init(): Promise<{ types: GeoObjectType[], hierarchies: HierarchyType[], locales: string[] }> {
		return this.http.get<{ types: GeoObjectType[], hierarchies: HierarchyType[], locales: string[] }>(acp + '/cgr/init')
			.toPromise();
	}

	// param types: array of GeoObjectType codes. If empty array then all GeoObjectType objects are returned.
	getGeoObjectTypes(types: string[], hierarchies: string[]): Promise<GeoObjectType[]> {
		let params: HttpParams = new HttpParams();


		if (types != null) {
			params = params.set('types', JSON.stringify(types));
		}

		if (hierarchies != null) {
			params = params.set('hierarchies', JSON.stringify(hierarchies));
		}

		return this.http
			.get<GeoObjectType[]>(acp + '/cgr/geoobjecttype/get-all', { params: params })
			.toPromise();
	}

	getParentGeoObjects(childId: string, childTypeCode: string, parentTypes: any, recursive: boolean, date: string): Promise<ParentTreeNode> {
		let params: HttpParams = new HttpParams();

		params = params.set('childId', childId)
		params = params.set('childTypeCode', childTypeCode)
		params = params.set('parentTypes', JSON.stringify(parentTypes))
		params = params.set('recursive', JSON.stringify(recursive));

		if (date != null) {
			params = params.set('date', date);
		}

		return this.http
			.get<ParentTreeNode>(acp + '/cgr/geoobject/get-parent-geoobjects', { params: params })
			.toPromise()
	}

	getChildGeoObjects(parentId: string, parentTypeCode: string, childrenTypes: any, recursive: boolean): Promise<ChildTreeNode> {
		let params: HttpParams = new HttpParams();

		params = params.set('parentId', parentId)
		params = params.set('parentTypeCode', parentTypeCode)
		params = params.set('childrenTypes', JSON.stringify(childrenTypes))
		params = params.set('recursive', JSON.stringify(recursive));

		return this.http
			.get<ChildTreeNode>(acp + '/cgr/geoobject/getchildren', { params: params })
			.toPromise();
	}

	newGeoObjectInstance(typeCode: string): Promise<any> {
		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return this.http
			.post<any>(acp + '/cgr/geoobject/newGeoObjectInstance', JSON.stringify({ 'typeCode': typeCode }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise();
	}

	createGeoObjectType(gtJSON: string): Promise<GeoObjectType> {

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return this.http
			.post<GeoObjectType>(acp + '/cgr/geoobjecttype/create', JSON.stringify({ 'gtJSON': gtJSON }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise();
	}

	updateGeoObjectType(gtJSON: GeoObjectType): Promise<GeoObjectType> {
		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return this.http
			.post<GeoObjectType>(acp + '/cgr/geoobjecttype/update', JSON.stringify({ "gtJSON": gtJSON }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise();
	}

	deleteGeoObjectType(code: string): Promise<void> {
		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return this.http
			.post<void>(acp + '/cgr/geoobjecttype/delete', JSON.stringify({ code: code }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise()
	}

	addAttributeType(geoObjTypeId: string, attribute: Attribute): Promise<Attribute> {

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return this.http
			.post<Attribute>(acp + '/cgr/geoobjecttype/addattribute', JSON.stringify({ 'geoObjTypeId': geoObjTypeId, 'attributeType': attribute }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise();
	}

	updateAttributeType(geoObjTypeId: string, attribute: Attribute): Promise<Attribute> {

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();


		return this.http
			.post<Attribute>(acp + '/cgr/geoobjecttype/updateattribute', JSON.stringify({ 'geoObjTypeId': geoObjTypeId, 'attributeType': attribute }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise();
	}

	deleteAttributeType(geoObjTypeId: string, attributeName: string): Promise<boolean> {

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();


		return this.http
			.post<boolean>(acp + '/cgr/geoobjecttype/deleteattribute', JSON.stringify({ 'geoObjTypeId': geoObjTypeId, 'attributeName': attributeName }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise();
	}

	addAttributeTermTypeOption(parentTermCode: string, term: Term): Promise<Term> {

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();


		return this.http
			.post<Term>(acp + '/cgr/geoobjecttype/addterm', JSON.stringify({ 'parentTermCode': parentTermCode, 'termJSON': term }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise();
	}

	updateAttributeTermTypeOption(parentTermCode: string, termJSON: Term): Promise<Term> {

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();


		return this.http
			.post<Term>(acp + '/cgr/geoobjecttype/updateterm', JSON.stringify({ 'parentTermCode': parentTermCode, 'termJSON': termJSON }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise();
	}

	deleteAttributeTermTypeOption(parentTermCode: string, termCode: string): Promise<Attribute> {

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();


		return this.http
			.post<Attribute>(acp + '/cgr/geoobjecttype/deleteterm', JSON.stringify({ 'parentTermCode': parentTermCode, 'termCode': termCode }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise();
	}

	getGeoObject(id: string, typeCode: string): Promise<GeoObject> {
		let params: HttpParams = new HttpParams();

		params = params.set('id', id)
		params = params.set('typeCode', typeCode);

		return this.http
			.get<GeoObject>(acp + '/cgr/geoobject/get', { params: params })
			.toPromise();
	}

	getGeoObjectBounds(code: string, typeCode: string): Promise<number[]> {
		let params: HttpParams = new HttpParams();

		params = params.set('code', code)
		params = params.set('typeCode', typeCode);

		return this.http
			.get<number[]>(acp + '/cgr/geoobject/get-bounds', { params: params })
			.toPromise();
	}

	getGeoObjectBoundsAtDate(code: string, typeCode: string, date: string): Promise<number[]> {
		let params: HttpParams = new HttpParams();

		params = params.set('code', code)
		params = params.set('typeCode', typeCode);
		params = params.set('date', date);

		return this.http
			.get<number[]>(acp + '/cgr/geoobject-time/get-bounds', { params: params })
			.toPromise();
	}

	getGeoObjectByCode(code: string, typeCode: string): Promise<GeoObject> {
		let params: HttpParams = new HttpParams();

		params = params.set('code', code)
		params = params.set('typeCode', typeCode);

		return this.http
			.get<GeoObject>(acp + '/cgr/geoobject/get-code', { params: params })
			.toPromise();
	}

	getHierarchiesForGeoObject(code: string, typeCode: string): Promise<HierarchyOverTime[]> {
		let params: HttpParams = new HttpParams();
		params = params.set('code', code);
		params = params.set('typeCode', typeCode);

		this.eventService.start();

		return this.http
			.get<HierarchyOverTime[]>(acp + '/cgr/geoobject/get-hierarchies-over-time', { params: params })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise()
	}

	getGeoObjectSuggestions(text: string, type: string, parent: string, hierarchy: string, date: string): Promise<GeoObject> {

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		let params = {
			text: text,
			type: type,
		} as any;

		if (parent != null && hierarchy != null) {
			params.parent = parent;
			params.hierarchy = hierarchy;
		}

		if (date != null) {
			params.date = date;
		}

		return this.http
			.post<GeoObject>(acp + '/cgr/geoobject/suggestions', JSON.stringify(params), { headers: headers })
			.toPromise();
	}

	getGeoObjectSuggestionsTypeAhead(text: string, type: string): Promise<GeoObject> {
		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		let params = {
			text: text,
			type: type,
		} as any;

		return this.http
			.post<GeoObject>(acp + '/cgr/geoobject/suggestions', JSON.stringify(params), { headers: headers })
			.toPromise();
	}

	getScheduledJobs(pageSize: number, pageNumber: number, sortAttr: string, isAscending: boolean): Promise<PaginationPage> {

		let params: HttpParams = new HttpParams();
		params = params.set('pageSize', pageSize.toString());
		params = params.set('pageNumber', pageNumber.toString());
		params = params.set('sortAttr', sortAttr);
		params = params.set('isAscending', isAscending.toString());


		return this.http
			.get<PaginationPage>(acp + '/etl/get-active', { params: params })
			.toPromise();
	}

	getCompletedScheduledJobs(pageSize: number, pageNumber: number, sortAttr: string, isAscending: boolean): Promise<PaginationPage> {

		let params: HttpParams = new HttpParams();
		params = params.set('pageSize', pageSize.toString());
		params = params.set('pageNumber', pageNumber.toString());
		params = params.set('sortAttr', sortAttr);
		params = params.set('isAscending', isAscending.toString());

		return this.http
			.get<PaginationPage>(acp + '/etl/get-completed', { params: params })
			.toPromise();
	}

	getScheduledJob(historyId: string, pageSize: number, pageNumber: number, onlyUnresolved: boolean): Promise<ScheduledJob> {
		let params: HttpParams = new HttpParams();
		params = params.set("historyId", historyId);
		params = params.set("pageSize", pageSize.toString());
		params = params.set("pageNumber", pageNumber.toString())
		params = params.set("onlyUnresolved", onlyUnresolved.toString());

		return this.http
			.get<ScheduledJob>(acp + '/etl/get-import-details', { params: params })
			.toPromise();
	}

	getExportDetails(historyId: string, pageSize: number, pageNumber: number): Promise<ScheduledJob> {
		let params: HttpParams = new HttpParams();
		params = params.set("historyId", historyId);
		params = params.set("pageSize", pageSize.toString());
		params = params.set("pageNumber", pageNumber.toString())

		return this.http
			.get<ScheduledJob>(acp + '/etl/get-export-details', { params: params })
			.toPromise();
	}

	resolveScheduledJob(historyId: string): Promise<void> {
		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return this.http
			.post<void>(acp + '/etl/import-resolve', JSON.stringify({ historyId: historyId }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise()
	}

	submitValidationResolve(config: any): Promise<any> {
		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return this.http
			.post<MasterList>(acp + '/etl/validation-resolve', JSON.stringify({ config: config }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise();
	}

	submitErrorResolve(config: any): Promise<any> {
		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return this.http
			.post<MasterList>(acp + '/etl/error-resolve', JSON.stringify({ config: config }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise();
	}

	getMasterLists(): Promise<{ locales: string[], lists: { label: string, oid: string, createDate: string, lastUpdateDate: string }[] }> {
		let params: HttpParams = new HttpParams();

		return this.http
			.get<{ locales: string[], lists: { label: string, oid: string, createDate: string, lastUpdateDate: string }[] }>(acp + '/master-list/list-all', { params: params })
			.toPromise();
	}

	getMasterListHistory(oid: string, versionType: string): Promise<MasterList> {
		let params: HttpParams = new HttpParams();
		params = params.set('oid', oid);
		params = params.set('versionType', versionType);

		return this.http
			.get<MasterList>(acp + '/master-list/versions', { params: params })
			.toPromise();
	}

	getAllMasterListVersions(): Promise<MasterList[]> {
		let params: HttpParams = new HttpParams();

		return this.http
			.get<MasterList[]>(acp + '/master-list/list-all-versions', { params: params })
			.toPromise();
	}

	getMasterListVersion(oid: string): Promise<MasterListVersion> {
		let params: HttpParams = new HttpParams();
		params = params.set('oid', oid);

		return this.http
			.get<MasterListVersion>(acp + '/master-list/version', { params: params })
			.toPromise();
	}

	getLocales(): Promise<string[]> {
		let params: HttpParams = new HttpParams();

		return this.http
			.get<string[]>(acp + '/localization/get-locales', { params: params })
			.toPromise();
	}

	getGeoObjectOverTime(geoObjectCode: string, geoObjectTypeCode: string): Promise<GeoObjectOverTime> {
		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		// let params: HttpParams = new HttpParams();
		// params = params.set( 'geoObjectCode', geoObjectCode );
		// params = params.set( 'geoObjectTypeCode', geoObjectTypeCode );
		// params = params.set( 'attributeName', attributeName );

		// return this.http
		//     .get<ValueOverTime[]>( acp + '/cgr/geoobject/getAttributeVersions', { params: params } )
		//     .toPromise();


		let params: HttpParams = new HttpParams();
		params = params.set('code', geoObjectCode);
		params = params.set('typeCode', geoObjectTypeCode);

		return this.http
			.get<GeoObjectOverTime>(acp + '/cgr/geoobject-time/get-code', { params: params })
			.toPromise();

	}

	newGeoObjectOverTime(typeCode: string): Promise<any> {
		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return this.http
			.post<any>(acp + '/cgr/geoobject-time/newGeoObjectInstance', JSON.stringify({ 'typeCode': typeCode }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise();
	}




	// getAttributeVersions( geoObjectCode: string, geoObjectTypeCode: string, attributeName: string ): any[] {
	//     let test =[
	//         {"startDate":1546300800000,"endDate":95649033600000,"value":{"localeValues":[
	//             {"locale":"defaultLocale","value":"Anlong Veaeng"}]}}
	//     ]

	//     // let test = [{"startDate":1546300800000,"endDate":95649033600000,"value":"test"}];

	//     return test;
	// }


	setAttributeVersions(geoObjectCode: string, geoObjectTypeCode: string, attributeName: string, collection: ValueOverTime[]): Promise<Response> {
		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		let params = {
			geoObjectCode: geoObjectCode,
			geoObjectTypeCode: geoObjectTypeCode,
			attributeName: attributeName,
			collection: collection

		} as any;

		this.eventService.start();

		return this.http
			.post<Response>(acp + '/cgr/geoobject/setAttributeVersions', JSON.stringify(params), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise();
	}

	createMasterList(list: MasterList): Promise<MasterList> {
		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return this.http
			.post<MasterList>(acp + '/master-list/create', JSON.stringify({ list: list }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise();
	}

	createMasterListVersion(oid: string, forDate: string): Promise<MasterListVersion> {
		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return this.http
			.post<MasterListVersion>(acp + '/master-list/create-version', JSON.stringify({ oid: oid, forDate: forDate }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise();
	}

	publishMasterListVersions(oid: string): Promise<{ job: string }> {
		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return this.http
			.post<{ job: string }>(acp + '/master-list/publish-versions', JSON.stringify({ oid: oid }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise();
	}

	deleteMasterList(oid: string): Promise<void> {
		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return this.http
			.post<void>(acp + '/master-list/remove', JSON.stringify({ oid: oid }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise()
	}

	deleteMasterListVersion(oid: string): Promise<void> {
		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return this.http
			.post<void>(acp + '/master-list/remove-version', JSON.stringify({ oid: oid }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise()
	}

	publishMasterList(oid: string): Observable<MasterListVersion> {
		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		return this.http.post<MasterListVersion>(acp + '/master-list/publish', JSON.stringify({ oid: oid }), { headers: headers });
	}

	getMasterList(oid: string): Promise<MasterList> {
		let params: HttpParams = new HttpParams();
		params = params.set('oid', oid);

		return this.http
			.get<MasterList>(acp + '/master-list/get', { params: params })
			.toPromise();
	}

    /*
     * Not really part of the RegistryService
     */
	applyGeoObjectEdit(parentTreeNode: HierarchyOverTime[], geoObject: GeoObjectOverTime, isNew: boolean, masterListId: string, notes: string): Promise<void> {
		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		let params = { parentTreeNode: parentTreeNode, geoObject: geoObject, isNew: isNew, masterListId: masterListId };

		if (notes != null) {
			params['notes'] = notes;
		}

		this.eventService.start();

		return this.http
			.post<void>(acp + '/geoobject-editor/apply', JSON.stringify(params), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise();
	}

	data(oid: string, pageNumber: number, pageSize: number, filter: { attribute: string, value: string }[], sort: { attribute: string, order: string }): Promise<any> {
		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		let params = {
			oid: oid,
			sort: sort
		} as any;

		if (pageNumber != null) {
			params.pageNumber = pageNumber;
		}

		if (pageSize != null) {
			params.pageSize = pageSize;
		}

		if (filter.length > 0) {
			params.filter = filter;
		}

		return this.http
			.post<any>(acp + '/master-list/data', JSON.stringify(params), { headers: headers })
			.toPromise();
	}

	values(oid: string, value: string, attributeName: string, valueAttribute: string, filter: { attribute: string, value: string }[]): Promise<{ label: string, value: string }[]> {
		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		let params = {
			oid: oid,
			attributeName: attributeName,
			valueAttribute: valueAttribute
		} as any;

		if (filter.length > 0) {
			params.filter = filter;
		}

		if (value != null && value.length > 0) {
			params.value = value;
		}


		return this.http
			.post<{ label: string, value: string }[]>(acp + '/master-list/values', JSON.stringify(params), { headers: headers })
			.toPromise();
	}

	publishShapefile(oid: string): Promise<MasterListVersion> {
		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		let params = {
			oid: oid
		} as any;

		return this.http
			.post<MasterListVersion>(acp + '/master-list/generate-shapefile', JSON.stringify(params), { headers: headers })
			.toPromise();
	}

	progress(oid: string): Promise<Progress> {
		let params: HttpParams = new HttpParams();
		params = params.set('oid', oid);

		return this.http
			.get<Progress>(acp + '/master-list/progress', { params: params })
			.toPromise();
	}

	getMasterListsByOrg(): Promise<{ locales: string[], orgs: MasterListByOrg[] }> {
		let params: HttpParams = new HttpParams();

		return this.http
			.get<{ locales: string[], orgs: MasterListByOrg[] }>(acp + '/master-list/list-org', { params: params })
			.toPromise();
	}

	getPublishMasterListJobs(oid: string, pageSize: number, pageNumber: number, sortAttr: string, isAscending: boolean): Promise<PaginationPage> {

		let params: HttpParams = new HttpParams();
		params = params.set('oid', oid);
		params = params.set('pageSize', pageSize.toString());
		params = params.set('pageNumber', pageNumber.toString());
		params = params.set('sortAttr', sortAttr);
		params = params.set('isAscending', isAscending.toString());


		return this.http
			.get<PaginationPage>(acp + '/master-list/get-publish-jobs', { params: params })
			.toPromise();
	}


	getOrganizations(): Promise<Organization[]> {

		this.eventService.start();

		return this.http
			.get<Organization[]>(acp + '/cgr/organizations/get-all')
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise();
	}


}
