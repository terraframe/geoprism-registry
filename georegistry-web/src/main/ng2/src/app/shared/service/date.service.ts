import { Injectable } from '@angular/core';

import { LocalizationService } from './localization.service';

import Utils from '../../registry/utility/Utils';

import { PRESENT, ValueOverTime } from '@registry/model/registry';

import * as moment from 'moment';

declare var Globalize: any;
declare var com: any
declare var registry: any

@Injectable()
export class DateService {

	constructor(private localizationService: LocalizationService) {}

	public getPresentDate(): Date {
		// new Date(5000, 12, 31) returns UTC time. 
		// new Date('5000-12-31') returns local time zone adjusted (e.g. off by one issues).
		// NOTE: Month is 0 indexed so 11 = December
		let dt =  new Date(5000, 11, 31, 0, 0, 0);
		
		return dt;
	}
	
	public formatDateForDisplay(date: string | Date): string {
		if(!date){
			return "";
		}
		
		
		if (date === PRESENT) {
			return this.localizationService.localize("changeovertime", "present");
		}
		
		if(date instanceof Date){
			return this.getDateString(date);
		}
		else{
			return date.split('T')[0];
		}
	}
	
	getDateString(date:Date): string {
		if(date instanceof Date){
			let year = date.getFullYear();
			let month:number|string = date.getMonth()+1;
			let dt:number|string = date.getDate();
			
			if (dt < 10) {
			  dt = '0' + dt;
			}
			if (month < 10) {
			  month = '0' + month;
			}
			
			return year + "-" + month + "-" + dt;
		}
		
		return null;
	}
	
	checkRanges(vAttributes: any): boolean {
		
		let hasConflict = false;
		
		// Check for overlaps
		for (let j = 0; j < vAttributes.length; j++) {
			const h1 = vAttributes[j];
			h1.conflictMessage = [];

			if (!(h1.startDate == null || h1.startDate === '') && !(h1.endDate == null || h1.endDate === '')) {
				let s1: any = new Date(h1.startDate);
				let e1: any = new Date(h1.endDate);

				if (Utils.dateEndBeforeStart(s1, e1)) {
					h1.conflictMessage.push({
						"type": "ERROR",	
						"message": this.localizationService.decode("manage.versions.startdate.later.enddate.message")
					});
					
					hasConflict = true;
				}

				for (let i = 0; i < vAttributes.length; i++) {

					if (j !== i) {
						const h2 = vAttributes[i];
						if (!(h2.startDate == null || h2.startDate === '') && !(h2.endDate == null || h2.endDate === '')) {
							let s2: any = new Date(h2.startDate);
							let e2: any = new Date(h2.endDate);

							// Determine if there is an overlap
							if (Utils.dateRangeOverlaps(s1.getTime(), e1.getTime(), s2.getTime(), e2.getTime())) {
								h1.conflictMessage.push({
									"type": "ERROR",	
									"message":this.localizationService.decode("manage.versions.overlap.message")
								});
								
								hasConflict = true;
							}
						}
					}
				}
			}
		}
		
		this.sort(vAttributes);
		
		// Check for gaps
		let current = null;
		for (let j = 0; j < vAttributes.length; j++) {
			let next = vAttributes[j];

			if (j > 0) {
				if (current.endDate && next.startDate) {
					let e1: any = new Date(current.endDate);
					let s2: any = new Date(next.startDate);

					if (Utils.hasGap(e1.getTime(), s2.getTime())) {
						next.conflictMessage.push({
							"type": "WARNING",	
							"message":this.localizationService.decode("manage.versions.gap.message")
						});
						
						current.conflictMessage.push({
							"type": "WARNING",	
							"message":this.localizationService.decode("manage.versions.gap.message")
						});
						
						hasConflict = true;
					}
				}
			}

			current = next;
		}
		
		return hasConflict;
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

}
