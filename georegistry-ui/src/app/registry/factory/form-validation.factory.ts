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

import { Directive } from "@angular/core";
import { NG_VALIDATORS, AbstractControl, ValidatorFn, Validator, FormControl } from "@angular/forms";

// validation function
function validateGeoObjectAttributeCodeValidator(): ValidatorFn {
    return (c: AbstractControl) => {
        if (c.value) {
            let isValid = c.value.indexOf(" ") === -1;

            if (isValid) {
                return null;
            } else {
                return {
                    geoObjectAttributeCode: {
                        valid: false
                    }
                };
            }
        }
    };
}

@Directive({
    selector: "[geoObjectAttributeCode][ngModel]",
    providers: [
        // eslint-disable-next-line no-use-before-define
        { provide: NG_VALIDATORS, useExisting: GeoObjectAttributeCodeValidator, multi: true }
    ]
})
export class GeoObjectAttributeCodeValidator implements Validator {

    validator: ValidatorFn;

    constructor() {
        this.validator = validateGeoObjectAttributeCodeValidator();
    }

    validate(c: FormControl) {
        return this.validator(c);
    }

}
