import { Pipe, PipeTransform } from '@angular/core';
import { LocalizationService } from '../service/localization.service';

@Pipe({name: 'localize'})
export class LocalizePipe implements PipeTransform {
  constructor(private service: LocalizationService) { }
	
  transform(value: string): string {
    return this.service.decode(value);
  }
}

