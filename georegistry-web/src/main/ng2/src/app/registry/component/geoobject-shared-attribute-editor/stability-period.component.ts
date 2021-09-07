import { Component, OnInit, Input } from '@angular/core';

import { GeoObjectOverTime, Attribute } from '@registry/model/registry';
import { LocalizationService } from '@shared/service';
import { DateService } from '@shared/service/date.service';

class Period {
  startDate: Date;
  endDate: Date;
}

/*
 * This component is shared between:
 * - 
 * 
 * Be wary of changing this component for one usecase and breaking other usecases!
 */
 @Component({
  selector: 'validity-period',
  templateUrl: './validity-period.component.html'
})
export class ValidityPeriodComponent implements OnInit {

  @Input() geoObjectOverTime: GeoObjectOverTime;

  periods: Period[] = [];

	constructor(private lService: LocalizationService, private dateService: DateService) {
	}

	ngOnInit(): void {
		this.generatePeriods();
	}
	
	generatePeriods()
	{
	  let len = this.geoObjectOverTime.geoObjectType.attributes.length;
	
	  for (let i = 0; i < len; ++i)
	  {
	    let attr: Attribute = this.geoObjectOverTime.geoObjectType.attributes[i];
	    
	    if (attr.isChangeOverTime)
	    {
	      let values = this.geoObjectOverTime.attributes[attr.code].values;
	      
	      let valLen = values.length;
	      for (let j = 0; j < valLen; ++j)
	      {
	        let period: Period = {startDate: new Date(values[j].startDate), endDate: new Date(values[j].endDate)};
	        //let period: Period = values[j];
	        this.addPeriod(period);
	      }
	    }
	  }
	}
	
	/**
	 * This algorithm can be thought of as being broken down into several separate
	 * "merge strategies" for merging a period into an existing range of periods.
	 *
	 * 1. Duplicate - This is the easiest. We do nothing because it's already accounted for
	 * 2. Subset - The period is a subset of an existing period in the collection. This, in turn breaks down into three distinct cases:
	 *             a. The start dates equal - Split into 2
	 *             b. The end dates equal - Split into 2
	 *             c. Neither start or end dates are equal -  Split into 3
	 * 3. Overlap - The period overlaps with the current
	 *             a. The end date is contained within the current - Create a new period before our current
	 *             b. The start date is contained within the current - Create a new period after our current
   * 4. Superset - The period completely contains the current
   *             a. Add a new period before and after the current
   * 5. No matches - The period has no interactions with anything in the existing set
   *             a. Add it and reorder
	 */
	addPeriod(period: Period)
	{
	  if (this.periods.length == 0)
	  {
	    this.periods.push(period);
	    return;
	  }
	  
	  let newPeriods = [];
	  let pushed = false;
	  
	  let len = this.periods.length;
	  for (let i = 0; i < len; ++i)
	  {
	    let current = this.periods[i];
	  
	    if (pushed)
	    {
	      newPeriods.push(current);
	      continue;
	    }
	    
	    let previous = i > 0 ? this.periods[i-1] : null;
	    let next = i < len-1 ? this.periods[i+i] : null;
	    
	    if (next == null && period.startDate >= current.endDate)
	    {
	      newPeriods.push(current);
	      newPeriods.push({startDate:this.addDay(1,period.startDate), endDate:period.endDate});
	      pushed = true;
	    }
	    else if (current.startDate === period.startDate && current.endDate === period.endDate)
	    {
	      // Duplicate
	    
	      return;
	    }
	    else if (period.startDate >= current.startDate && period.endDate <= current.endDate) 
	    {
	      // Subset
	    
	      if (period.startDate != current.startDate && period.endDate != current.endDate)
	      {
	        newPeriods.push({startDate:current.startDate, endDate:this.addDay(-1, period.startDate)});
	        newPeriods.push(period);
	        newPeriods.push({startDate:this.addDay(1, period.endDate), endDate:current.endDate});
	      }
	      else if (period.startDate === current.startDate)
	      {
          newPeriods.push(period);
          newPeriods.push({startDate:this.addDay(1, period.endDate), endDate:current.endDate});
	      }
	      else
        {
          newPeriods.push({startDate:current.startDate, endDate:this.addDay(-1, period.startDate)});
          newPeriods.push(period);
        }
	      
	      pushed = true;
	    }
      else if (period.startDate >= current.startDate && period.startDate <= current.endDate) {
          // Start date overlap
          
          newPeriods.push(current);
          newPeriods.push({startDate:this.addDay(1, current.endDate), endDate:period.endDate});
          pushed = true;
      } else if (period.endDate >= current.startDate && period.endDate <= current.endDate) {
          // End date overlap
          
          newPeriods.push({startDate:period.startDate, endDate:this.addDay(-1, current.startDate)});
          newPeriods.push(current);
          pushed = true;
      } else if (period.startDate < current.startDate && current.endDate < period.endDate) {
          // Superset
          
          newPeriods.push({startDate:period.startDate, endDate:this.addDay(-1, current.startDate)});
          newPeriods.push(current);
          newPeriods.push({startDate:this.addDay(1, current.endDate), endDate:period.endDate});
          pushed = true;
      }
	  }
	  
	  if (!pushed)
	  {
	    newPeriods.push(period);
	  }
	  
	  this.periods = newPeriods;
	}
	
	addDay(amount: number, date: Date)
	{
	  let plus1: Date = new Date(date.getTime());
    plus1.setUTCDate(date.getUTCDate() + amount);
    return plus1;
	}
	
	formatDate(date: string): string {
    return this.dateService.formatDateForDisplay(date);
  }

}
