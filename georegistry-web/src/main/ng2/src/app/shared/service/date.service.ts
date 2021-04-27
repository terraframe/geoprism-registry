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
	overlapMessage = { 
		"type": "ERROR",	
		"message":this.localizationService.decode("manage.versions.overlap.message")
	}
	
	gapMessage = {
		"type": "WARNING",	
		"message":this.localizationService.decode("manage.versions.gap.message")
	}

	constructor(private localizationService: LocalizationService) {}

	// Get infinity date (called 'present' in the UI)
	public getPresentDate(): Date {
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
	
	// @param value as yyyy-mm-dd
	getDateFromDateString(value: string){
		return new Date(+value.split("-")[0], +value.split("-")[1]-1, +value.split("-")[2]);
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
	
	checkRanges(vAttributes: any[]): boolean {
		
		let hasConflict = false;
		
		// clear all messages
		vAttributes.forEach(attr => {
			attr.conflictMessage = [];
		})
		
		// Check for overlaps
		for (let j = 0; j < vAttributes.length; j++) {
			const h1 = vAttributes[j];

			if (h1.startDate && h1.endDate) {
				let s1: any = this.getDateFromDateString(h1.startDate);
				let e1: any = this.getDateFromDateString(h1.endDate);

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
						
						// If all dates set
						if (h2.startDate && h2.endDate) {
							let s2: Date = this.getDateFromDateString(h2.startDate);
							let e2: Date = this.getDateFromDateString(h2.endDate);

							// Determine if there is an overlap
							if (Utils.dateRangeOverlaps(s1.getTime(), e1.getTime(), s2.getTime(), e2.getTime())) {
								h1.conflictMessage.push(this.overlapMessage);
								
								if(s2.getTime() === e2.getTime()){
									h2.conflictMessage.push(this.overlapMessage);
								}
								
								hasConflict = true;
							}
						}
						// If 1st end date and current start date
						else if( (i === j-1 || i === j+1) && e1 && h2.startDate){
							let s2: Date = this.getDateFromDateString(h2.startDate);
							
							if(s2.getTime() <= e1){
								
								h1.conflictMessage.push(this.overlapMessage);
								h2.conflictMessage.push(this.overlapMessage);
								
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
						next.conflictMessage.push(this.gapMessage);
						
						current.conflictMessage.push(this.gapMessage);
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
