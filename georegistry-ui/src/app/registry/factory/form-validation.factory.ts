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
