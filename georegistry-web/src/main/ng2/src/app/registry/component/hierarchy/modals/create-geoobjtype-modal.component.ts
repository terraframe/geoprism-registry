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
	organization: Organization = null;
	message: string = null;
	parents: GeoObjectType[];
	hierarchyType: HierarchyType;
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

	init(organization: Organization, parents: GeoObjectType[], groupSuperType: GeoObjectType, isAbstract: boolean) {

		this.geoObjectType.isAbstract = isAbstract ? isAbstract : false;

		if (groupSuperType) {
			this.geoObjectType.superTypeCode = groupSuperType.code;
			this.geoObjectType.geometryType = groupSuperType.geometryType;
			this.geoObjectType.isPrivate = groupSuperType.isPrivate;
		}

		// Filter out parents that are not abstract
		this.parents = parents.filter(parent => parent.isAbstract);

		// Filter out organizations they're not RA's of
		this.organization = organization;
		this.geoObjectType.organizationCode = this.organization.code;
		this.organizationLabel = this.organization.label.localizedValue;
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
