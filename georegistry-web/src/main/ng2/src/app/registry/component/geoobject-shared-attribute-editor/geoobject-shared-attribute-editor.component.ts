import { Component, OnInit, ViewChild, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { DatePipe } from '@angular/common';
import {
	trigger,
	style,
	animate,
	transition,
	state
} from '@angular/animations';

import { LocalizedValue } from '@shared/model/core';
import { LocalizationService } from '@shared/service';
import { AuthService } from '@shared/service';

import { ManageVersionsModalComponent } from './manage-versions-modal.component';

import { GeoObjectType, GeoObjectOverTime, Attribute, AttributeTerm, AttributeDecimal, Term, PRESENT } from '@registry/model/registry';

import Utils from '../../utility/Utils';


@Component({
	selector: 'geoobject-shared-attribute-editor',
	templateUrl: './geoobject-shared-attribute-editor.component.html',
	styleUrls: ['./geoobject-shared-attribute-editor.css'],
	providers: [DatePipe],
	animations: [
		[
			trigger('fadeInOut', [
				transition(':enter', [
					style({
						opacity: 0
					}),
					animate('500ms')
				]),
				transition(':leave',
					animate('500ms', 
						style({
							opacity: 0
						})
					)
				)
			]),
			trigger('slide', [
				state('left', style({ left: 0 })),
      			state('right', style({ left: '100%' })),
      			transition('* => *', animate(200))
			])
	]]
})

/**
 * This component is shared between:
 * - crtable (create-update-geo-object action detail)
 * - change-request (for submitting change requests)
 * - master list geoobject editing widget
 * 
 * Be wary of changing this component for one usecase and breaking other usecases!
 */
export class GeoObjectSharedAttributeEditorComponent implements OnInit, OnChanges {
	
	isContributorOnly: boolean = false;

	private bsModalRef: BsModalRef;
	
	@Input() animate: boolean = false;

    /*
	 * The current state of the GeoObject in the GeoRegistry
	 */
	@Input() preGeoObject: GeoObjectOverTime = null;

	/*
 	* The state of the GeoObject being modified
 	*/
	@Input() postGeoObject: GeoObjectOverTime = null;


	calculatedPreObject: any = {};

	calculatedPostObject: any = {};

	@Input() geoObjectType: GeoObjectType;

	@Input() attributeExcludes: string[] = [];

	@Input() forDate: Date = new Date();

	@Input() readOnly: boolean = false;

	@Input() isNew: boolean = false;

	@Input() isEditingGeometries = false;

	@Input() isGeometryInlined = false;

	@ViewChild("geometryEditor") geometryEditor;

	@Output() valid = new EventEmitter<boolean>();

	@Output() onManageVersion = new EventEmitter<Attribute>();

    /*
     * Observable subject for MasterList changes.  Called when an update is successful 
     */
	@Output() onChange = new EventEmitter<GeoObjectOverTime>()

	@Input() customEvent: boolean = false;

	modifiedTermOption: Term = null;
	currentTermOption: Term = null;
	isValid: boolean = true;

	geoObjectAttributeExcludes: string[] = ["uid", "sequence", "type", "lastUpdateDate", "createDate"];

	@ViewChild("attributeForm") attributeForm;

	constructor(private modalService: BsModalService, private lService: LocalizationService, private authService: AuthService) {
		this.isContributorOnly = this.authService.isContributerOnly()
	}

	ngOnInit(): void {
		
		if (this.attributeExcludes != null) {
			this.geoObjectAttributeExcludes.push.apply(this.geoObjectAttributeExcludes, this.attributeExcludes);

			if (!this.isGeometryInlined) {
				this.geoObjectAttributeExcludes.push("geometry");
			}
		}

		let geomAttr = null;
		for (var i = 0; i < this.geoObjectType.attributes.length; ++i) {
			if (this.geoObjectType.attributes[i].code === 'geometry') {
				geomAttr = this.geoObjectType.attributes[i];
			}
		}
		if (geomAttr == null) {
			let geometry: Attribute = new Attribute("geometry", "geometry", new LocalizedValue("Geometry", null), new LocalizedValue("Geometry", null), true, false, false, true);
			this.geoObjectType.attributes.push(geometry);
		}
	}

	ngAfterViewInit() {
		this.attributeForm.statusChanges.subscribe(result => {
			this.isValid = (result === "VALID" || result === "DISABLED");

			this.valid.emit(this.isValid);
		});
	}

	ngOnChanges(changes: SimpleChanges) {
		
		if (changes['preGeoObject']) {

			this.preGeoObject = new GeoObjectOverTime(this.geoObjectType, JSON.parse(JSON.stringify(this.preGeoObject)).attributes); // We're about to heavily modify this object. We don't want to muck with the original copy they sent us.

			if (this.postGeoObject == null) {
				this.postGeoObject = new GeoObjectOverTime(this.geoObjectType, JSON.parse(JSON.stringify(this.preGeoObject)).attributes); // Object.assign is a shallow copy. We want a deep copy.
			}
			else {
				this.postGeoObject = new GeoObjectOverTime(this.geoObjectType, JSON.parse(JSON.stringify(this.postGeoObject)).attributes); // We're about to heavily modify this object. We don't want to muck with the original copy they sent us.
			}

			this.calculate();
		}
		else if (changes['forDate']) {
			this.calculate();
		}
	}
	
	setBoolean(attribute, value): void {
		attribute.value = value
	}

	calculate(): void {
		this.calculatedPreObject = this.calculateCurrent(this.preGeoObject);
		this.calculatedPostObject = this.calculateCurrent(this.postGeoObject);
		
		if (this.geometryEditor != null) {
			this.geometryEditor.reload();
		}
	}

	calculateCurrent(goot: GeoObjectOverTime): any {
		const object = {};

		const time = this.forDate.getTime();

		for (let i = 0; i < this.geoObjectType.attributes.length; ++i) {
			let attr = this.geoObjectType.attributes[i];
			object[attr.code] = null;

			if (attr.type === 'local') {
				object[attr.code] =
				{
					startDate: null,
					endDate: null,
					value: this.lService.create()
				};
			}

			if (attr.isChangeOverTime) {
				let values = goot.attributes[attr.code].values;

				values.forEach(vot => {

					const startDate = Date.parse(vot.startDate);
					const endDate = Date.parse(vot.endDate);

					if (time >= startDate && time <= endDate) {

						if (attr.type === 'local') {
							object[attr.code] = {
								startDate: this.formatDate(vot.startDate),
								endDate: this.formatDate(vot.endDate),
								value: JSON.parse(JSON.stringify(vot.value))
							};
						}
						else if (attr.type === 'term' && vot.value != null && Array.isArray(vot.value) && vot.value.length > 0) {
							object[attr.code] = {
								startDate: this.formatDate(vot.startDate),
								endDate: this.formatDate(vot.endDate),
								value: vot.value[0]
							};
						}
						else {
							object[attr.code] = {
								startDate: this.formatDate(vot.startDate),
								endDate: this.formatDate(vot.endDate),
								value: vot.value
							};
						}
					}
				});
			}
			else {
				object[attr.code] = goot.attributes[attr.code];
			}
		}

		for (let i = 0; i < this.geoObjectType.attributes.length; ++i) {
			let attr = this.geoObjectType.attributes[i];

			if (attr.isChangeOverTime && object[attr.code] == null) {
				object[attr.code] = {
					startDate: null,
					endDate: null,
					value: ""
				}
			}
		}

		return object;
	}

	formatDate(date: string): string {
		return this.lService.formatDateForDisplay(date);
	}

	handleChangeCode(e: any): void {
		this.postGeoObject.attributes.code = this.calculatedPostObject['code'];
		
		this.onChange.emit(this.postGeoObject);
	}

	onManageAttributeVersions(attribute: Attribute): void {

		if (this.customEvent) {
			this.onManageVersion.emit(attribute);
		}
		else {
			this.bsModalRef = this.modalService.show(ManageVersionsModalComponent, {
				animated: true,
				backdrop: true,
				ignoreBackdropClick: true,
			});

			
			this.bsModalRef.content.geoObjectOverTime = this.postGeoObject;
			this.bsModalRef.content.geoObjectType = this.geoObjectType;
			this.bsModalRef.content.isNewGeoObject = this.isNew;
			this.bsModalRef.content.attribute = attribute;
			this.bsModalRef.content.onAttributeVersionChange.subscribe(versionObj => {
				this.calculate();
			});
			//this.bsModalRef.content.readonly = !this.isContributorOnly;
			this.bsModalRef.content.readonly = this.readOnly;
		}
	}

	onManageGeometryVersions(): void {
		let geometry = null;
		for (var i = 0; i < this.geoObjectType.attributes.length; ++i) {
			if (this.geoObjectType.attributes[i].code === 'geometry') {
				geometry = this.geoObjectType.attributes[i];
			}
		}

		this.onManageAttributeVersions(geometry);
	}

	isDifferentText(attribute: Attribute): boolean {
		if (this.calculatedPostObject[attribute.code] == null && this.calculatedPreObject[attribute.code] != null) {
			return true;
		}

		return (this.calculatedPostObject[attribute.code].value && this.calculatedPostObject[attribute.code].value.trim() !== this.calculatedPreObject[attribute.code].value);
	}

	isDifferentValue(attribute: Attribute): boolean {
		if (this.calculatedPostObject[attribute.code] == null && this.calculatedPreObject[attribute.code] != null) {
			return true;
		}

		return (this.calculatedPostObject[attribute.code].value && this.calculatedPostObject[attribute.code].value !== this.calculatedPreObject[attribute.code].value);
	}

	onSelectPropertyOption(event: any, option: any): void {
		this.currentTermOption = JSON.parse(JSON.stringify(this.modifiedTermOption));
	}

	getGeoObjectTypeTermAttributeOptions(termAttributeCode: string) {
		for (let i = 0; i < this.geoObjectType.attributes.length; i++) {
			let attr: any = this.geoObjectType.attributes[i];

			if (attr.type === "term" && attr.code === termAttributeCode) {

				attr = <AttributeTerm>attr;
				let attrOpts = attr.rootTerm.children;

				if (attr.code === "status") {
					return Utils.removeStatuses(attrOpts);
				}
				else {
					return attrOpts;
				}
			}
		}

		return null;
	}

	isStatusChanged(post, pre) {

		if (pre != null && post == null) {
			return true;
		}

		if (pre == null || post == null || pre.length == 0 || post.length == 0) {
			return false;
		}

		var preCompare = pre;
		if (Array.isArray(pre)) {
			preCompare = pre[0];
		}

		var postCompare = post;
		if (Array.isArray(post)) {
			postCompare = post[0];
		}

		return preCompare !== postCompare;
	}

	getTypeDefinition(key: string): string {

		for (let i = 0; i < this.geoObjectType.attributes.length; i++) {
			let attr = this.geoObjectType.attributes[i];

			if (attr.code === key) {
				return attr.type;
			}
		}

		return null;
	}

	public getIsValid(): boolean {
		return this.isValid;
	}

	public getGeoObject(): any {
		return this.postGeoObject;
	}
}
