import { Component, OnInit } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs';
import { HttpErrorResponse } from "@angular/common/http";
import { ErrorHandler } from '@shared/component';
import { GeoObjectType } from '@registry/model/registry';

import { RegistryService } from '@registry/service';
import { LocalizationService, AuthService } from '@shared/service';
import { Organization } from '@shared/model/core';
import { HierarchyType } from '@registry/model/hierarchy';

@Component({
	selector: 'create-geoobjtype-modal',
	templateUrl: './create-geoobjtype-modal.component.html',
	styleUrls: []
})
export class CreateGeoObjTypeModalComponent implements OnInit {

	geoObjectType: GeoObjectType;
	organizations: Organization[] = [];
	message: string = null;
	parents: GeoObjectType[];
	hierarchyType:HierarchyType;
	organizationLabel: string;

    /*
     * Observable subject for TreeNode changes.  Called when create is successful 
     */
	public onGeoObjTypeCreate: Subject<GeoObjectType>;

	constructor(private lService: LocalizationService, private auth: AuthService, private registryService: RegistryService, public bsModalRef: BsModalRef) { }

	ngOnInit(): void {
		this.onGeoObjTypeCreate = new Subject();

		this.geoObjectType = {
			"code": "",
			"label": this.lService.create(),
			"description": this.lService.create(),
			"geometryType": "MULTIPOINT",
			"isLeaf": false,
			"isGeometryEditable": true,
			"organizationCode": "",
			"attributes": [],
		};
	}

	init(organizations: Organization[], parents: GeoObjectType[], groupSuperType: GeoObjectType, isAbstract: boolean) {
		
		this.geoObjectType.superTypeCode = groupSuperType ? groupSuperType.code : "";
		this.geoObjectType.isAbstract = isAbstract ? isAbstract : false;
		this.geoObjectType.geometryType = groupSuperType.geometryType;
		
		// Filter out parents that are not abstract
		this.parents = parents.filter(parent => parent.isAbstract);

		// Filter out organizations they're not RA's of
		this.organizations = [];

		for (var i = 0; i < organizations.length; ++i) {
			if (this.auth.isOrganizationRA(organizations[i].code)) {
				this.organizations.push(organizations[i]);
			}
		}

		if (this.organizations.length === 1) {
			this.geoObjectType.organizationCode = this.organizations[0].code;
			this.organizationLabel = this.organizations[0].label.localizedValue;
		}
	}

	getOrganizationLabelFromCode(orgCode: string) {
		for (var i = 0; i < this.organizations.length; ++i) {
			if (this.organizations[i].code === orgCode) {
				return this.organizations[i].label.localizedValue;
			}
		}

		console.log("Did not find org with code [" + orgCode + "]");
		return orgCode;
	}

	handleOnSubmit(): void {
		this.message = null;

		this.registryService.createGeoObjectType(JSON.stringify(this.geoObjectType)).then(data => {
			this.onGeoObjTypeCreate.next(data);
			this.bsModalRef.hide();
		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});

	}

	toggleIsLeaf(): void {
		this.geoObjectType.isLeaf = !this.geoObjectType.isLeaf;
	}

	toggleIsGeometryEditable(): void {
		this.geoObjectType.isGeometryEditable = !this.geoObjectType.isGeometryEditable;
	}

	toggleIsAbstract(): void {
		this.geoObjectType.isAbstract = !this.geoObjectType.isAbstract;
	}

	error(err: HttpErrorResponse): void {
		this.message = ErrorHandler.getMessageFromError(err);
	}
}
