import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'fill'
})
export class FillPipe implements PipeTransform {
  transform(value:any) {
    return (new Array(Math.floor(value))).fill(1);
  }
}