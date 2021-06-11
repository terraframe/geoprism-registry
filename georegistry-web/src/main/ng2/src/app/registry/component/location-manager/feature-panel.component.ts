import { Component, OnInit, Input, Output, EventEmitter, OnDestroy } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';

import { GeoObjectType, GeoObjectOverTime, Attribute, HierarchyOverTime, ValueOverTime } from '@registry/model/registry';
import { RegistryService } from '@registry/service';
import { AuthService } from '@shared/service';
import { ErrorModalComponent, ErrorHandler } from '@shared/component';
import { Subject } from 'rxjs';

@Component({
	selector: 'feature-panel',
	templateUrl: './feature-panel.component.html',
	styleUrls: ['./dataset-location-manager.css']
})
export class FeaturePanelComponent implements OnInit {
	MODE = {
		VERSIONS: 'VERSIONS',
		ATTRIBUTES: 'ATTRIBUTES',
		HIERARCHY: 'HIERARCHY',
		GEOMETRY: 'GEOMETRY'
	}

	@Input() datasetId: string;

	@Input() type: GeoObjectType;

	@Input() forDate: Date = new Date();

	@Input() readOnly: boolean = false;

	@Input() set code(value: string) {
		this.updateCode(value);
	}

	_code: string = null;

	@Input() geometryChange: Subject<any>;

	@Output() geometryEdit = new EventEmitter<ValueOverTime>();
	@Output() featureChange = new EventEmitter<GeoObjectOverTime>();
	@Output() modeChange = new EventEmitter<boolean>();
	@Output() panelCancel = new EventEmitter<void>();
	@Output() panelSubmit = new EventEmitter<{isChangeRequest:boolean, geoObject?: any, changeRequestId?: string}>();
	
	isValid: boolean = true;

	bsModalRef: BsModalRef;

	mode: string = null;

	isMaintainer: boolean;

	// The current state of the GeoObject in the GeoRegistry
	preGeoObject: GeoObjectOverTime;

	// The state of the GeoObject after our edit has been applied
	postGeoObject: GeoObjectOverTime;

	attribute: Attribute = null;

	isNew: boolean = false;

	isEdit: boolean = true;

	hierarchies: HierarchyOverTime[];

	hierarchy: HierarchyOverTime = null;
	
	reason: string = "";

	constructor(public service: RegistryService, private modalService: BsModalService, private authService: AuthService) {
	}

	ngOnInit(): void {
	  this.isMaintainer = this.authService.isSRA() || this.authService.isOrganizationRA(this.type.organizationCode) || this.authService.isGeoObjectTypeOrSuperRM(this.type);
		this.mode = 'ATTRIBUTES';
		this.geometryChange.subscribe(v => {
			this.updateGeometry(v);
		});
	}
	
	setValid(valid: boolean): void {
		this.isValid = valid;
	}

	updateCode(code: string): void {
		this._code = code;
		this.postGeoObject = null;
		this.preGeoObject = null;
		this.hierarchies = null;
		
		//this.setEditMode(false);
		this.setEditMode(true);

		if (code != null && this.type != null) {

			if (code !== '__NEW__') {
				this.isNew = false;

				this.service.getGeoObjectOverTime(code, this.type.code).then(geoObject => {
					this.preGeoObject = new GeoObjectOverTime(this.type, JSON.parse(JSON.stringify(geoObject)).attributes);
					this.postGeoObject = new GeoObjectOverTime(this.type, JSON.parse(JSON.stringify(this.preGeoObject)).attributes);
				}).catch((err: HttpErrorResponse) => {
					this.error(err);
				});

				this.service.getHierarchiesForGeoObject(code, this.type.code).then((hierarchies: HierarchyOverTime[]) => {
					this.hierarchies = hierarchies;
				}).catch((err: HttpErrorResponse) => {
					this.error(err);
				});
			}
			else {
				this.isNew = true;

				this.service.newGeoObjectOverTime(this.type.code).then(retJson => {
					this.preGeoObject = new GeoObjectOverTime(this.type, retJson.geoObject.attributes);
					this.postGeoObject = new GeoObjectOverTime(this.type, JSON.parse(JSON.stringify(this.preGeoObject)).attributes);

					this.hierarchies = retJson.hierarchies;
					this.setEditMode(true);
				});
			}

		}
	}
	

	onEditGeometryVersion(vot: ValueOverTime): void {
		this.geometryEdit.emit(vot);
	}

	updateGeometry(geometry: any): void {
		// Check if the geometry has been updated
		if (geometry != null && this.postGeoObject != null) {

			let values = this.postGeoObject.attributes['geometry'].values;
			const time = this.forDate.getTime();

			values.forEach(vot => {

				const startDate = Date.parse(vot.startDate);
				const endDate = Date.parse(vot.endDate);

				if (time >= startDate && time <= endDate) {
					vot.value = geometry;
				}
			});
		}
	}

	calculateGeometry(goot: GeoObjectOverTime): any {

		const time = this.forDate.getTime();

		let values = goot.attributes['geometry'].values;

		for (let i = 0; i < values.length; i++) {
			const vot = values[i];

			const startDate = Date.parse(vot.startDate);
			const endDate = Date.parse(vot.endDate);

			if (time >= startDate && time <= endDate) {
				return vot.value;
			}
		};

		return null;
	}

	onCancelInternal(): void {

    this.panelCancel.emit();


		//if (this._code === '__NEW__') {
		//	this.updateCode(null);
		//}
		//else {
		//	this.updateCode(this._code);
		//}
	}

	onSubmit(): void {
		this.service.applyGeoObjectEdit(this.hierarchies, this.postGeoObject, this.isNew, this.datasetId, this.reason).then((applyInfo: any) => {
		  if (!applyInfo.isChangeRequest)
      {
			  this.featureChange.emit(this.postGeoObject);
			  this.updateCode(this._code);
			}
			this.panelSubmit.emit(applyInfo);
		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});
	}

	onManageAttributeVersion(attribute: Attribute): void {
		this.attribute = attribute;
		this.mode = this.MODE.VERSIONS;
	}

	onManageHiearchyVersion(hierarchy: HierarchyOverTime): void {
		this.hierarchy = hierarchy;
		this.mode = this.MODE.HIERARCHY;
	}

	onAttributeChange(postGeoObject: GeoObjectOverTime): void {
		
		this.postGeoObject = postGeoObject;

		this.mode = this.MODE.ATTRIBUTES;

		this.geometryEdit.emit(null);
	}

	onHierarchyChange(hierarchy: HierarchyOverTime): void {
		const index = this.hierarchies.findIndex(h => h.code === hierarchy.code);
		if (index !== -1) {
			this.hierarchies[index] = hierarchy;
		}

		this.mode = this.MODE.ATTRIBUTES;
	}

	onEditAttributes(): void {
		this.setEditMode(!this.isEdit);
	}

	setEditMode(value: boolean): void {
		this.isEdit = value;
		this.reason = null;

		this.modeChange.emit(this.isEdit)
	}

	public error(err: HttpErrorResponse): void {
		this.bsModalRef = ErrorHandler.showErrorAsDialog(err, this.modalService);
	}

}
