import { 
	Component, 
	OnInit, 
	Input, 
	Output, 
	EventEmitter, 
	ViewChildren, 
	QueryList } from '@angular/core';

import {
	trigger,
	style,
	animate,
	transition,
} from '@angular/animations';

import { Observable } from 'rxjs';
import { TypeaheadMatch } from 'ngx-bootstrap/typeahead';

import { HierarchyOverTime, PRESENT, ValueOverTime } from '@registry/model/registry';

import{ DateFieldComponent } from '../../../shared/component/form-fields/date-field/date-field.component';

import { RegistryService } from '@registry/service';
import { DateService } from '@shared/service/date.service';
import { LocalizationService } from '@shared/service';

import * as moment from 'moment';

@Component({
	selector: 'manage-parent-versions',
	templateUrl: './manage-parent-versions.component.html',
	styleUrls: [],
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
export class ManageParentVersionsComponent implements OnInit {
	
	@ViewChildren('dateFieldComponents') dateFieldComponentsArray:QueryList<DateFieldComponent>;
	
	currentDate : Date =new Date();
	
	isValid: boolean = true;

	originalHierarchy: HierarchyOverTime;
	@Input() hierarchy: HierarchyOverTime = null;

	@Output() onChange = new EventEmitter<HierarchyOverTime>();

	hasDuplicateDate: boolean = false;

	loading: any = {};

	constructor(private service: RegistryService, private localizeService: LocalizationService, private dateService: DateService) { }

	ngOnInit(): void {

		this.originalHierarchy = JSON.parse(JSON.stringify(this.hierarchy));

		this.hierarchy.entries.forEach(entry => {
			for (let i = 0; i < this.hierarchy.types.length; i++) {
				let current = this.hierarchy.types[i];

				if (entry.parents[current.code] == null) {
					entry.parents[current.code] = { text: '', geoObject: null };
				}

				entry.loading = {};
			}
		});
	}

	onAddNewVersion(): void {

		let parents = {};

		for (let i = 0; i < this.hierarchy.types.length; i++) {
			let current = this.hierarchy.types[i];

			parents[current.code] = {};
		}

		const entry = {
			startDate: null,
			endDate: null,
			parents: parents,
			loading: {}
		}

		this.hierarchy.entries.push(entry);
	}

	remove(entry: any): void {

		for (let i = 0; i < this.hierarchy.entries.length; i++) {
			let vals = this.hierarchy.entries[i];

			if (vals.startDate === entry.startDate) {
				this.hierarchy.entries.splice(i, 1);
			}
		}

		this.onDateChange();		
	}

	getTypeAheadObservable(date: string, type: any, entry: any, index: number): Observable<any> {

		let geoObjectTypeCode = type.code;

		let parentCode = null;
		let parentTypeCode = null;
		let hierarchyCode = null;

		if (index > 0) {
			let pType = this.hierarchy.types[index - 1];
			const parent = entry.parents[pType.code];

			if (parent.geoObject != null && parent.geoObject.properties.code != null) {
				hierarchyCode = this.hierarchy.code;
				parentCode = parent.geoObject.properties.code;
				parentTypeCode = parent.geoObject.properties.type;
			}
		}

		return Observable.create((observer: any) => {
		  if (parentCode == null)
		  {
		    let loopI = index;
		  
		    while (parentCode == null && loopI > 0)
		    {
		      loopI = loopI - 1;
		      
		      let parent = entry.parents[this.hierarchy.types[loopI].code];
		      
		      if (parent != null)
		      {
		        if (parent.geoObject != null && parent.geoObject.properties.code != null)
		        {
              parentCode = parent.geoObject.properties.code;
              hierarchyCode = this.hierarchy.code;
              parentTypeCode = this.hierarchy.types[loopI].code;
            }
            else if (parent.goCode != null)
            {
              parentCode = parent.goCode;
              hierarchyCode = this.hierarchy.code;
              parentTypeCode = this.hierarchy.types[loopI].code;
            }
		      }
		    }
		  }
		
			this.service.getGeoObjectSuggestions(entry.parents[type.code].text, geoObjectTypeCode, parentCode, parentTypeCode, hierarchyCode, date).then(results => {
				observer.next(results);
			});
		});
	}

	typeaheadOnSelect(e: TypeaheadMatch, type: any, entry: any, date: string): void {
		//        let ptn: ParentTreeNode = parent.ptn;

    	entry.parents[type.code].text = e.item.name + " : " + e.item.code;
   		entry.parents[type.code].goCode = e.item.code;

		let parentTypes = [];

		for (let i = 0; i < this.hierarchy.types.length; i++) {
			let current = this.hierarchy.types[i];

			parentTypes.push(current.code);

			if (current.code === type.code) {
				break;
			}
		}

		this.service.getParentGeoObjects(e.item.uid, type.code, parentTypes, true, date).then(ancestors => {

      	delete entry.parents[type.code].goCode;
			entry.parents[type.code].geoObject = ancestors.geoObject;
			entry.parents[type.code].text = ancestors.geoObject.properties.displayLabel.localizedValue + ' : ' + ancestors.geoObject.properties.code;

			for (let i = 0; i < this.hierarchy.types.length; i++) {
				let current = this.hierarchy.types[i];
				let ancestor = ancestors;

				while (ancestor != null && ancestor.geoObject.properties.type != current.code) {
					if (ancestor.parents.length > 0) {
						ancestor = ancestor.parents[0];
					}
					else {
						ancestor = null;
					}
				}

				if (ancestor != null) {
					entry.parents[current.code].geoObject = ancestor.geoObject;
					entry.parents[current.code].text = ancestor.geoObject.properties.displayLabel.localizedValue + ' : ' + ancestor.geoObject.properties.code;
				}
			}

		});
	}

	onRemove(type: any, entry: any): void {
		entry.parents[type.code].text = '';
		delete entry.parents[type.code].geoObject;
		delete entry.parents[type.code].goCode;
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
		
		this.isValid = this.checkDateFieldValidity();

		this.dateService.checkRanges(this.hierarchy.entries);
	}

	formatDateString(dateObj: Date): string {
		const day = dateObj.getUTCDate();

		return dateObj.getUTCFullYear() + "-" + (dateObj.getUTCMonth() + 1) + "-" + (day < 10 ? "0" : "") + day;
	}

	formatDate(date: string) {
		let localeData = moment.localeData(date);
		var format = localeData.longDateFormat('L');
		return moment().format(format);
		
//		return this.localizeService.formatDateForDisplay(date);
	}
	
	setInfinity(vAttribute, hierarchyOverTime: HierarchyOverTime): void {
		
		if(vAttribute.endDate === PRESENT){
			vAttribute.endDate = new Date();
		}
		else{
			vAttribute.endDate = PRESENT
		}
		
		this.onDateChange();
	}
	
	sort(hierarchyOverTime: HierarchyOverTime): void {
		
		// Sort the data by start date 
		hierarchyOverTime.entries.sort(function(a, b) {

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
		this.onChange.emit(this.hierarchy);
	}

	onCancel(): void {
		this.onChange.emit(this.originalHierarchy);
	}
}
