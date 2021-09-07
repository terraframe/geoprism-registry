import { Pipe, PipeTransform } from "@angular/core";

@Pipe({
    name: "geoObjectAttributeExcludesFilter",
    pure: true
})
export class GeoObjectAttributeExcludesPipe implements PipeTransform {

    transform(items: any[], filter: string[]): any {
        if (!items || !filter) {
            return items;
        }

        return items.filter(item => filter.indexOf(item.code) === -1)
    }
}