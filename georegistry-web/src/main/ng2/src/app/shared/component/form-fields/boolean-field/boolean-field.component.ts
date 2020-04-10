import {Component, Input, Output, EventEmitter} from '@angular/core';

declare var acp:string;

@Component({    
  selector: 'boolean-field',
  templateUrl: './boolean-field.component.html',
  styleUrls: ['./boolean-field.css']
})
export class BooleanFieldComponent {

  @Input() value:boolean = false;
  @Output() public valueChange = new EventEmitter<boolean>();

  @Input() localizeLabelKey:string = ""; // localization key used to localize in the component template
  @Input() label:string = ""; // raw string input

  @Input() disable:boolean = false;

  /* You can pass a function in with (change)='function()' */
  @Output() public change = new EventEmitter<any>();

  constructor(){}
  
  toggle():void {
    if(!this.disable){
      this.value = !this.value;
      
      this.valueChange.emit(this.value);
      this.change.emit(this.value);
    }
  }
}