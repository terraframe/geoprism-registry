import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'toEpochDateTime',
    pure: true
})
export class ToEpochDateTimePipe implements PipeTransform {
    transform(date: string): any {
        if (!date ) {
            return date;
        }
       
        return new Date(date).getTime();
    }
}