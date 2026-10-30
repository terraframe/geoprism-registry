///
/// Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Geoprism Registry(tm).
///
/// Geoprism Registry(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Geoprism Registry(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
///

import { Directive, Input } from '@angular/core';
import { AbstractControl, NG_VALIDATORS, Validator, ValidationErrors } from '@angular/forms';
import { IdMapping } from '@registry/model/io';

@Directive({
  selector: '[idMappingUniqueValue]',
  standalone: true, // Use standalone: true for modern Angular (v14+)
  providers: [
    {
      provide: NG_VALIDATORS,
      useExisting: UniqueMappingValidatorDirective,
      multi: true
    }
  ]
})
export class UniqueMappingValidatorDirective implements Validator {
  // Pass the list of existing values to check against
  @Input('idMappingUniqueValue') existingValues: IdMapping[] = [];

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
