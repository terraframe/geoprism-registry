import {
  Component,
  Optional,
  Inject,
  Input,
  ViewChild,
} from '@angular/core';

import {
  NgModel,
  NG_VALUE_ACCESSOR,
  NG_VALIDATORS,
  NG_ASYNC_VALIDATORS,
} from '@angular/forms';
 
import {ElementBase, animations, ValidationComponent} from '../base';

@Component({
  selector: 'input-field',
  templateUrl: './input-field.component.html',
  styleUrls: ['./input-field.css'],
  animations,
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: InputFieldComponent,
    multi: true,
  }],
})
export class InputFieldComponent extends ElementBase<string> {
  @Input() public label: string = "";
  @Input() public placeholder: string = "";

  @ViewChild(NgModel) model: NgModel;

  public identifier = `form-text-${identifier++}`;

  constructor(
    @Optional() @Inject(NG_VALIDATORS) validators: Array<any>,
    @Optional() @Inject(NG_ASYNC_VALIDATORS) asyncValidators: Array<any>,
  ) {
    super(validators, asyncValidators);
  }
}

let identifier = 0;