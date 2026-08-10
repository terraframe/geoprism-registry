import { Directive, Input } from '@angular/core';
import { AbstractControl, NG_VALIDATORS, Validator, ValidationErrors } from '@angular/forms';
import { IdMapping } from '@registry/model/io';

@Directive({
  selector: '[authorityUniqueValue]',
  standalone: true, // Use standalone: true for modern Angular (v14+)
  providers: [
    {
      provide: NG_VALIDATORS,
      useExisting: UniqueAuthorityValidatorDirective,
      multi: true
    }
  ]
})
export class UniqueAuthorityValidatorDirective implements Validator {
  // Pass the list of existing values to check against
  @Input('authorityUniqueValue') existingValues: IdMapping[] = [];

  validate(control: AbstractControl): ValidationErrors | null {
    if (!control.value) {
      return null;
    }

    // Check if the current value already exists in the provided array
    const isNotUnique = this.existingValues.some(
      (val) => val.authority === control.value
    );

    // Return an error object if not unique, or null if valid
    return isNotUnique ? { uniqueValue: { actualValue: control.value } } : null;
  }
}
