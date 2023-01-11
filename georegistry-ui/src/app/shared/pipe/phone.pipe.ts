import { Pipe, PipeTransform } from "@angular/core";
import { formatNumber, ParsedNumber } from "libphonenumber-js";

@Pipe({
    name: "phone"
})
export class PhonePipe implements PipeTransform {

    transform(value: ParsedNumber, args?: string): any {
        if (!value) {
            return value;
        }

        return formatNumber({ country: "US", phone: value.toString() }, "International");
    }

}
