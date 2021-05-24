import { 
	Component, 
	OnInit, 
	Input, 
	Output, 
	ChangeDetectorRef, 
	EventEmitter, 
	ViewChildren, 
	QueryList } from '@angular/core';
import {
	trigger,
	style,
	animate,
	transition,
} from '@angular/animations';

import { GeoObjectType, Attribute, ValueOverTime, GeoObjectOverTime, PRESENT } from '@registry/model/registry';

import{ DateFieldComponent } from '../../../shared/component/form-fields/date-field/date-field.component';

import { LocalizationService } from '@shared/service';
import { DateService } from '@shared/service/date.service';

import * as moment from 'moment';


@Component({
	selector: 'geometry-panel',
	templateUrl: './geometry-panel.component.html',
	styleUrls: ['./geometry-panel.css'],
	host: { '[@fadeInOut]': 'true' },
	animations: [
		[
			trigger('fadeInOut', [
				transition('void => *', [
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
			])
		]]
})
export class GeometryPanelComponent implements OnInit {
	
	@ViewChildren('dateFieldComponents') dateFieldComponentsArray:QueryList<DateFieldComponent>;
	
	currentDate : Date = new Date();
	
	isValid: boolean = true;
	
	isVersionForHighlight: number;
	
	message: string = null;

	readonly: boolean = false;
	
	hasConflict: boolean = false;

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

	constructor(private lService: LocalizationService, public changeDetectorRef: ChangeDetectorRef, private dateService: DateService) { }

	ngOnInit(): void {
	}

	checkDateFieldValidity(): boolean {
		let dateFields = this.dateFieldComponentsArray.toArray();
		
		for(let i=0; i<dateFields.length; i++){
			let field = dateFields[i];
			if(!field.valid){
				return false;
			}
		}
		
		return true;
	}
	
	onDateChange(): any {
		this.hasConflict = false;
		
		this.isValid = this.checkDateFieldValidity();
	
		let vAttributes = this.geoObjectOverTime.attributes['geometry'].values;

		this.hasConflict = this.dateService.checkRanges(vAttributes);
	}

	edit(vot: ValueOverTime, isVersionForHighlight: number): void {
		this.onEdit.emit(vot);
		
		this.isVersionForHighlight = isVersionForHighlight;
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

		let position = -1;
		for (let i = 0; i < val.values.length; i++) {
			let vals = val.values[i];


			if (vals.startDate === version.startDate) {
				position = i
			}
		}
		
		if(position > -1){
			val.values.splice(position, 1);
		}
		
	}
	
	formatDate(date: string) {
		let localeData = moment.localeData(date);
  		var format = localeData.longDateFormat('L');
  		return moment().format(format);
	}
	
	setInfinity(vAttribute, attributes): void {
		
		if(vAttribute.endDate === PRESENT){
			vAttribute.endDate = new Date();
		}
		else{
			vAttribute.endDate = PRESENT
		}
		
		this.onDateChange();
		
	}
	
	sort(votArr: ValueOverTime[]): void {

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
	}

	onSubmit(): void {
		this.onChange.emit(this.geoObjectOverTime);
	}

	onCancel(): void {
		this.onChange.emit(this.originalGeoObjectOverTime);
	}
}
