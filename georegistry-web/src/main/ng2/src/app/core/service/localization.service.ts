///
/// Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Runway SDK(tm).
///
/// Runway SDK(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Runway SDK(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Runway SDK(tm).  If not, see <ehttp://www.gnu.org/licenses/>.
///

import { Injectable } from '@angular/core';

declare var Globalize: any;
declare var com: any

@Injectable()
export class LocalizationService {

  private parser: any = Globalize.numberParser();
  private formatter: any = Globalize.numberFormatter();
    
  public parseNumber(value: string) : number {
    if(value != null && value.length > 0) {            
      //convert data from view format to model format
      var number = this.parser( value );
        
      return number;
    }
          
    return null;
  }    
    
  public formatNumber(value:any): string {
    if(value != null) {
      var number = value;
            
      if(typeof number === 'string') {
        if(number.length > 0 && Number(number)) {
          number = Number(value);            
        }
        else {
          return "";
        }
      }
            
      //convert data from model format to view format
      return this.formatter(number);
    }
            
    return null;
  }
    
  public localize(bundle: string, key: string): string {
    return com.runwaysdk.Localize.localize(bundle, key);
  }
    
  public get(key: string): string {
    return com.runwaysdk.Localize.get(key);
  }
  
  public decode(key: string): string {
    let index = key.lastIndexOf('.');
    
    if(index !== -1) {
      
      let temp = [key.slice(0, index), key.slice(index + 1)]
    
      return this.localize(temp[0], temp[1]);
    }
    else {
      return this.get(key);
    }
  }
}
