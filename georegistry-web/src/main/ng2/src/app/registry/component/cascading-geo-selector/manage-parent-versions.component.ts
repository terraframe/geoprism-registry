import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { Observable } from 'rxjs';
import { TypeaheadMatch } from 'ngx-bootstrap/typeahead';

import { HierarchyOverTime, PRESENT } from '@registry/model/registry';

import { RegistryService } from '@registry/service';

import * as moment from 'moment';
import Utils from '@registry/utility/Utils';

@Component({
	selector: 'manage-parent-versions',
	templateUrl: './manage-parent-versions.component.html',
	styleUrls: []
})
export class ManageParentVersionsComponent implements OnInit {

	originalHierarchy: HierarchyOverTime;
	@Input() hierarchy: HierarchyOverTime = null;

	@Output() onChange = new EventEmitter<HierarchyOverTime>();

	hasDuplicateDate: boolean = false;

	loading: any = {};

	constructor(private service: RegistryService) { }

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

		//		this.snapDates();
		this.onDateChange();		
	}

	getTypeAheadObservable(date: string, type: any, entry: any, index: number): Observable<any> {

		let geoObjectTypeCode = type.code;

		let parentCode = null;
		let hierarchyCode = null;

		if (index > 0) {
			let pType = this.hierarchy.types[index - 1];
			const parent = entry.parents[pType.code];

			if (parent.geoObject != null && parent.geoObject.properties.code != null) {
				hierarchyCode = this.hierarchy.code;
				parentCode = parent.geoObject.properties.code;
			}
		}

		return Observable.create((observer: any) => {
			this.service.getGeoObjectSuggestions(entry.parents[type.code].text, geoObjectTypeCode, parentCode, hierarchyCode, date).then(results => {
				observer.next(results);
			});
		});
	}

	typeaheadOnSelect(e: TypeaheadMatch, type: any, entry: any, date: string): void {
		//        let ptn: ParentTreeNode = parent.ptn;

		let parentTypes = [];

		for (let i = 0; i < this.hierarchy.types.length; i++) {
			let current = this.hierarchy.types[i];

			parentTypes.push(current.code);

			if (current.code === type.code) {
				break;
			}
		}

		this.service.getParentGeoObjects(e.item.uid, type.code, parentTypes, true, date).then(ancestors => {

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
	}

	onDateChange(): any {
		//		this.snapDates();

		// check ranges
		for (let j = 0; j < this.hierarchy.entries.length; j++) {
			const h1 = this.hierarchy.entries[j];
			h1.conflict = false;

			if (!(h1.startDate == null || h1.startDate === '') && !(h1.endDate == null || h1.endDate === '')) {
				let s1: any = new Date(h1.startDate);
				let e1: any = new Date(h1.endDate);

				for (let i = 0; i < this.hierarchy.entries.length; i++) {

					if (j !== i) {
						const h2 = this.hierarchy.entries[i];
						if (!(h2.startDate == null || h2.startDate === '') && !(h2.endDate == null || h2.endDate === '')) {
							let s2: any = new Date(h2.startDate);
							let e2: any = new Date(h2.endDate);

							// Determine if there is an overlap
							if (Utils.dateRangeOverlaps(s1.getTime(), e1.getTime(), s2.getTime(), e2.getTime())) {
								h1.conflict = true;
							}
						}
					}
				}
			}
		}
	}

	snapDates() {
		var dateOffset = (24 * 60 * 60 * 1000) * 1; //1 days

		this.hasDuplicateDate = false;

		// Sort the data
		this.hierarchy.entries.sort(function(a, b) {

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


		for (let i = 1; i < this.hierarchy.entries.length; i++) {
			let prev = this.hierarchy.entries[i - 1];
			let current = this.hierarchy.entries[i];

			prev.endDate = this.formatDateString(new Date(new Date(current.startDate).getTime() - dateOffset));

			if (prev.startDate === current.startDate) {
				this.hasDuplicateDate = true;
			}
		}

		if (this.hierarchy.entries.length > 0) {
			this.hierarchy.entries[this.hierarchy.entries.length - 1].endDate = PRESENT;
		}
	}

	formatDateString(dateObj: Date): string {
		const day = dateObj.getUTCDate();

		return dateObj.getUTCFullYear() + "-" + (dateObj.getUTCMonth() + 1) + "-" + (day < 10 ? "0" : "") + day;
	}

	formatDate(date: string) {
		let localeData = moment.localeData(date);
		var format = localeData.longDateFormat('L');
		return moment().format(format);
	}

	onSubmit(): void {
		this.onChange.emit(this.hierarchy);
	}

	onCancel(): void {
		this.onChange.emit(this.originalHierarchy);
	}
}
