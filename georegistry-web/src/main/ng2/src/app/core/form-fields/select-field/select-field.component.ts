import { Component, Input, Output, EventEmitter, Optional, Inject, ViewChild } from '@angular/core';
import {
  NgModel,
  NG_VALUE_ACCESSOR,
  NG_VALIDATORS,
  NG_ASYNC_VALIDATORS,
} from '@angular/forms';
import {ElementBase, animations} from '../base';
import { LocalizationService } from '../../../core/service/localization.service';


declare var acp: string;

export class SelectValue {
    value: string;
    localizedLabel: string;

    constructor(value: string, label: string) {
        this.value = value;
        this.localizedLabel = label;
    }
}

@Component({
    selector: 'select-field',
    templateUrl: './select-field.component.html',
    styles: ['./select-field.css']
})
export class SelectFieldComponent extends ElementBase<string> {
    @Input() public label: string;
    @Input() public placeholder: string;

    @ViewChild(NgModel) model: NgModel;

    public identifier = `form-select-${identifier++}`;

    constructor(
        @Optional() @Inject(NG_VALIDATORS) validators: Array<any>,
        @Optional() @Inject(NG_ASYNC_VALIDATORS) asyncValidators: Array<any>,
        private localizeService: LocalizationService
    ) {
        super(validators, asyncValidators);

    }

    ngOnInit(){
        if(this.placeholder){
          this.placeholder = this.localizeService.decode(this.placeholder);
        }

        if(this.label){
          this.label = this.localizeService.decode(this.label);
        }
    }
}

let identifier = 0;