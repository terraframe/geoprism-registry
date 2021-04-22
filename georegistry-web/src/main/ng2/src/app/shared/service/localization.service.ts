import { Injectable } from '@angular/core';
import { LocalizedValue } from '@shared/model/core';

import { PRESENT } from '@registry/model/registry';

import * as moment from 'moment';

declare var Globalize: any;
declare var com: any
declare var registry: any

@Injectable()
export class LocalizationService {

	locales: string[] = ['defaultLocale'];
	locale: string;

	private parser: any = Globalize.numberParser();
	private formatter: any = Globalize.numberFormatter();

	constructor() {
		this.locales = registry.locales;
		this.locale = registry.locale;
	}

	getLocales(): string[] {
		return this.locales;
	}

	getLocale(): string {
		return this.locale;
	}

	setLocales(locales: string[]): void {
		// The installed locales are now read from the global registry value on load
		//		this.locales = locales;
	}

	addLocale(locale: string): void {

		if (this.locales.indexOf(locale) === -1) {
			this.locales.push(locale);
		}
	}

	create(): LocalizedValue {
		const value = { localizedValue: '', localeValues: [] } as LocalizedValue;

		this.locales.forEach(locale => {
			value.localeValues.push({ locale: locale, value: '' });
		});

		return value;
	}

	public parseNumber(value: string): number {
		if (value != null && value.length > 0) {
			//convert data from view format to model format
			var number = this.parser(value);

			return number;
		}

		return null;
	}

	public formatNumber(value: any): string {
		if (value != null) {
			var number = value;

			if (typeof number === 'string') {
				if (number.length > 0 && Number(number)) {
					number = Number(value);
				}
				else {
					return "";
				}
			}

			//convert data from model format to view format
			return this.formatter(number);
		}

		return null;
	}
	
	public getPresentDate(): Date {
		// new Date(5000, 12, 31) returns UTC time. 
		// new Date('5000-12-31') returns local time zone adjusted (e.g. off by one issues).
		
		let dt =  new Date(5000, 11, 31, 0, 0, 0);
		
		return dt;
	}
	
	public formatDateForDisplay(date: string | Date): string {
		if(!date){
			return "";
		}
		
		
		if (date === PRESENT) {
			return this.localize("changeovertime", "present");
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

	public localize(bundle: string, key: string): string {
		return com.runwaysdk.Localize.localize(bundle, key);
	}

	public get(key: string): string {
		return com.runwaysdk.Localize.get(key);
	}

	public decode(key: string): string {
		let index = key.lastIndexOf('.');

		if (index !== -1) {

			let temp = [key.slice(0, index), key.slice(index + 1)]

			return this.localize(temp[0], temp[1]);
		}
		else {
			return this.get(key);
		}
	}
}
