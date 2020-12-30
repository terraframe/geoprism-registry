export default class Utils {
	
	/**
	 * 
	 * @param arr 
	 */
	static removeStatuses( arr: any[] ): any[] {
	      var newArray = [];
	      
	      for ( var i = 0; i < arr.length; ++i ) {
	        if (! (arr[i].code === "CGR:Status-New"
	             || arr[i].code === "CGR:Status-Pending"))
	        {
	          newArray.push(arr[i]);
	        }
	      }
	      
	      return newArray;
	}
	

	/**
	 * 
	 * @param dateObj 
	 */
	static formatDateString(dateObj: Date): string {
        const day = dateObj.getUTCDate();
        const month = dateObj.getUTCMonth() + 1;

        return dateObj.getUTCFullYear() + "-" + ( month < 10 ? "0" : "" ) + month + "-" + ( day < 10 ? "0" : "" ) + day;
    }

	static dateRangeOverlaps(a_start: number, a_end: number, b_start: number, b_end: number): boolean {
		if (a_start <= b_start && b_start <= a_end) return true; // b starts in a
		if (a_start <= b_end && b_end <= a_end) return true; // b ends in a
		if (b_start < a_start && a_end < b_end) return true; // a in b
		return false;
	}


    
}