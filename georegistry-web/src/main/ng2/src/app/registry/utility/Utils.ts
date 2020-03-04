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
        const month = dateObj.getUTCMonth() + 1;

        return dateObj.getUTCFullYear() + "-" + ( month < 10 ? "0" : "" ) + month + "-" + ( day < 10 ? "0" : "" ) + day;
    }
    
    static getFriendlyProblemType(probType: string): string {
        if(probType === "net.geoprism.registry.io.ParentCodeException"){
            return "Parent Lookup";
        }

        if(probType === "net.geoprism.registry.io.PostalCodeLocationException"){
            return "Postal Code Lookup";
        }

        if(probType === "net.geoprism.registry.io.AmbiguousParentException"){
            return "Ambiguous Parent";
        }

        if(probType === "net.geoprism.registry.io.InvalidGeometryException"){
            return "Invalid Geometry";
        }

        if(probType === "net.geoprism.registry.DataNotFoundException"){
            return "Empty Value Where Required";
        }

        if(probType === "net.geoprism.registry.io.TermValueException"){
            // TODO
        }

        return "";
    }
}