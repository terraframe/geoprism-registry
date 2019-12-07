export default class Utils {
	
	/**
	 * 
	 * @param arr 
	 */
	static removeStatuses( arr: any[] ): any[] {
        var newI = -1;
        for ( var i = 0; i < arr.length; ++i ) {
            if ( arr[i].code === "CGR:Status-New" ) {
                newI = i;
                break;
            }
        }
        if ( newI != -1 ) {
            arr.splice( newI, 1 );
        }


        var pendI = 0;
        for ( var i = 0; i < arr.length; ++i ) {
            if ( arr[i].code === "CGR:Status-Pending" ) {
                pendI = i;
                break;
            }
        }
        if ( pendI != -1 ) {
            arr.splice( pendI, 1 );
        }

        return arr;
	}
	

	/**
	 * 
	 * @param dateObj 
	 */
	static formatDateString(dateObj: Date): string {
        const day = dateObj.getUTCDate();

        return dateObj.getUTCFullYear() + "-" + ( dateObj.getUTCMonth() + 1 ) + "-" + ( day < 10 ? "0" : "" ) + day;
    }
    
}