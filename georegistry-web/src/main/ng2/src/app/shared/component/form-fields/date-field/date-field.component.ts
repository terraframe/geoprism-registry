import {Component, Input, Output, EventEmitter} from '@angular/core';
import { LocalizationService} from '@shared/service';

import * as moment from 'moment';

declare var acp:string;

@Component({    
  selector: 'date-field',
  templateUrl: './date-field.component.html',
  styleUrls: ['./date-field.css']
})
export class DateFieldComponent {
	
  @Input() allowFutureDates:boolean = true;
 
  @Input() inputName:string = this.idGenerator();

  @Input() classNames:string[] = [];

  @Input() value:Date;
  @Output() public valueChange = new EventEmitter<string>();

  @Input() localizeLabelKey:string = ""; // localization key used to localize in the component template
  @Input() label:string = ""; // raw string input

  @Input() disable:boolean = false;

  @Input() required:boolean = false;

  /* You can pass a function in with (change)='function()' */
  @Output() public change = new EventEmitter<any>();

  today: Date = new Date();

  message:string;

  valid:boolean = true;
  @Output() public isValid = new EventEmitter<boolean>();

  constructor(private localizationService: LocalizationService){}

  idGenerator() {
    var S4 = function() {
       return (((1+Math.random())*0x10000)|0).toString(16).substring(1);
    };
    return (S4()+S4()+"-"+S4()+"-"+S4()+"-"+S4()+"-"+S4()+S4()+S4());
  }

  toggle(event: any):void {
	this.value = new Date(event.target.value);
	
	this.valid = true;
	this.message = "";

	if(!this.allowFutureDates && this.value > this.today) {
	    this.valid = false;
		this.message = this.localizationService.decode("date.inpu.data.in.future.error.message");
		
		event.target.value = this.today.toISOString().substr(0, 10);
	}
	else if( !(this.value instanceof Date) ) {
		this.valid = false;
		
		this.message = "Invalid date"
		
		event.target.value = this.today.toISOString().substr(0, 10);
	}
	else if( isNaN(Number(this.value)) ) {
		this.valid = false;
		
		this.message = "Not a valid number"
		
		event.target.value = this.today.toISOString().substr(0, 10);
	}
	

	if(this.valid){
		// Must adhere to the ISO 8601 format
		let formattedDate = moment(this.value, "YYYY-MM-DD").toISOString().split('T')[0];
	
		this.valueChange.emit(formattedDate);
    	this.change.emit(formattedDate);
	}
	else{
		this.valueChange.emit(this.today.toISOString().substr(0, 10));
    	this.change.emit(this.today.toISOString().substr(0, 10));
	}
	
	this.isValid.emit(this.valid);
	
  }
}