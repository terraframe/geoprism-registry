export default class Utils {

    /**
     *
     * @param arr
     */
    static removeStatuses(arr: any[]): any[] {
        var newArray = [];

        for (var i = 0; i < arr.length; ++i) {
            if (!(arr[i].code === "CGR:Status-New" ||
                arr[i].code === "CGR:Status-Pending")) {
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

        return dateObj.getUTCFullYear() + "-" + (month < 10 ? "0" : "") + month + "-" + (day < 10 ? "0" : "") + day;
    }

    static dateRangeOverlaps(aStart: number, aEnd: number, bStart: number, bEnd: number): boolean {
        if (aStart <= bStart && bStart <= aEnd) return true; // b starts in a
        if (aStart <= bEnd && bEnd <= aEnd) return true; // b ends in a
        if (bStart < aStart && aEnd < bEnd) return true; // a in b
        return false;
    }

    static dateRangeOutside(aStart: number, aEnd: number, bStart: number, bEnd: number): boolean {
        if (aStart < bStart) return true;
        if (aEnd > bEnd) return true;
        return false;
    }

    static dateEndBeforeStart(start: number, end: number): boolean {
        if (end < start) return true; // end date is before start date
        return false;
    }

    static hasGap(aEnd: number, bStart: number): boolean {
        return (bStart - aEnd) > (1000 * 60 * 60 * 24);
    }

}
