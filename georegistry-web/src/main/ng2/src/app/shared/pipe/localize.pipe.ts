import { Pipe, PipeTransform } from "@angular/core";
import { LocalizationService } from "@shared/service";

@Pipe({ name: "localize" })
export class LocalizePipe implements PipeTransform {

    // eslint-disable-next-line no-useless-constructor
    constructor(private service: LocalizationService) { }

    transform(value: string): string {
        return this.service.decode(value);
    }

}
