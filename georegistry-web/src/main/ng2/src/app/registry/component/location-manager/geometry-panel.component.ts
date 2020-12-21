import { Component, OnInit, Input, Output, ChangeDetectorRef, EventEmitter } from '@angular/core';
import {
	trigger,
	style,
	animate,
	transition,
} from '@angular/animations';

import { GeoObjectType, Attribute, ValueOverTime, GeoObjectOverTime, PRESENT } from '@registry/model/registry';

import { LocalizationService } from '@shared/service';

import Utils from '../../utility/Utils';

import * as moment from 'moment';


@Component({
	selector: 'geometry-panel',
	templateUrl: './geometry-panel.component.html',
	styleUrls: [],
	host: { '[@fadeInOut]': 'true' },
	animations: [
		[
			trigger('fadeInOut', [
				transition('void => *', [
					style({
						opacity: 0
					}),
					animate('1000ms')
				]),
				transition('* => void', [
					style({
						opacity: 0
					}),
					animate('1000ms')
				])
			])
		]]
})
export class GeometryPanelComponent implements OnInit {
	message: string = null;

	readonly: boolean = false;

    /*
     * Observable subject for MasterList changes.  Called when an update is successful 
     */
	@Output() onChange = new EventEmitter<GeoObjectOverTime>()

	@Output() onEdit = new EventEmitter<ValueOverTime>()

	@Input() geoObjectType: GeoObjectType;

	originalGeoObjectOverTime: GeoObjectOverTime;
	geoObjectOverTime: GeoObjectOverTime;
	
	@Input() set geoObjectOverTimeInput(value: GeoObjectOverTime) {
		this.originalGeoObjectOverTime = JSON.parse(JSON.stringify(value));
		this.geoObjectOverTime = value;
	}

	@Input() isNewGeoObject: boolean = false;

	goGeometries: GeoObjectOverTime;

	newVersion: ValueOverTime;

	hasDuplicateDate: boolean = false;

	constructor(private lService: LocalizationService, public changeDetectorRef: ChangeDetectorRef) { }

	ngOnInit(): void {
	}

	onDateChange(event: any, vAttribute: ValueOverTime): any {

		//        console.log( event.currentTarget.value );
		//
		//        let dt = new Date( event.currentTarget.value );
		//let dt = new Date(event);

		let vAttributes = this.geoObjectOverTime.attributes['geometry'].values;

		//        vAttribute.startDate = Utils.formatDateString( dt );

		this.snapDates(vAttributes);

		//        this.changeDetectorRef.detectChanges();
	}

	snapDates(votArr: ValueOverTime[]): void {
		var dateOffset = (24 * 60 * 60 * 1000) * 1; //1 days

		this.hasDuplicateDate = false;

		// Sort the data by start date 
		votArr.sort(function(a, b) {

			if (a.startDate == null || a.startDate === '') {
				return 1;
			}
			else if (b.startDate == null || b.startDate === '') {
				return -1;
			}

			let first: any = new Date(a.startDate);
			let next: any = new Date(b.startDate);
			return first - next;
		});

		for (let i = 1; i < votArr.length; i++) {
			let prev = votArr[i - 1];
			let current = votArr[i];

			if (current.startDate) {
				prev.endDate = Utils.formatDateString(new Date(new Date(current.startDate).getTime() - dateOffset));
			}
			else {
				prev.endDate = PRESENT;
			}

			if (prev.startDate === current.startDate) {
				this.hasDuplicateDate = true;
			}
		}

		if (votArr.length > 0) {
			votArr[votArr.length - 1].endDate = PRESENT;
		}
	}

	edit(vot: ValueOverTime): void {
		this.onEdit.emit(vot);
	}

	onAddNewVersion(): void {
		let votArr: ValueOverTime[] = this.geoObjectOverTime.attributes['geometry'].values;

		let vot: ValueOverTime = new ValueOverTime();
		vot.startDate = null;  // Utils.formatDateString(new Date());
		vot.endDate = null;  // Utils.formatDateString(new Date());

		vot.value = { "type": this.geoObjectType.geometryType, "coordinates": [] };

		if (this.geoObjectType.geometryType === "MULTIPOLYGON") {
			vot.value.type = "MultiPolygon";
		}
		else if (this.geoObjectType.geometryType === "POLYGON") {
			vot.value.type = "Polygon";
		}
		else if (this.geoObjectType.geometryType === "POINT") {
			vot.value.type = "Point";
		}
		else if (this.geoObjectType.geometryType === "MULTIPOINT") {
			vot.value.type = "MultiPoint";
		}
		else if (this.geoObjectType.geometryType === "LINE") {
			vot.value.type = "Line";
		}
		else if (this.geoObjectType.geometryType === "MULTILINE") {
			vot.value.type = "MultiLine";
		}

		votArr.push(vot);

		this.snapDates(votArr);

		this.changeDetectorRef.detectChanges();
	}

	getVersionData(attribute: Attribute) {
		let versions: ValueOverTime[] = [];

		this.geoObjectOverTime.attributes[attribute.code].values.forEach(vAttribute => {
			vAttribute.value.localeValues.forEach(val => {
				versions.push(val);
			})
		})
		return versions;
	}

	getDefaultLocaleVal(locale: any): string {
		let defVal = null;

		locale.localeValues.forEach(locVal => {
			if (locVal.locale === 'defaultLocale') {
				defVal = locVal.value;
			}

		})

		return defVal;
	}

	setDateAttribute(vot: ValueOverTime, val: string): void {
		vot.value = new Date(val).getTime().toString()
	}

	remove(version: any): void {

		let val = this.geoObjectOverTime.attributes['geometry'];

		for (let i = 0; i < val.values.length; i++) {
			let vals = val.values[i];

			if (vals.startDate === version.startDate) {
				val.values.splice(i, 1);
			}
		}

		this.snapDates(val.values);
	}
	
	formatDate(date: string) {
		let localeData = moment.localeData(date);
  		var format = localeData.longDateFormat('L');
  		return moment().format(format);
	}

	onSubmit(): void {
		this.onChange.emit(this.geoObjectOverTime);
	}

	onCancel(): void {
		this.onChange.emit(this.originalGeoObjectOverTime);
	}
}
