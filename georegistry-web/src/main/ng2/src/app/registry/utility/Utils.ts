import { AttributeType, GeoObject } from "@registry/model/registry";
import { LocalizedValue } from "@shared/model/core";
// eslint-disable-next-line camelcase
import turf_booleanequal from "@turf/boolean-equal";

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

    public static areValuesEqual(attributeType: AttributeType, val1: any, val2: any): boolean {
        if (attributeType.type === "boolean") {
            return val1 === val2;
        }

        if ((val1 === "" && val2 == null) || (val2 === "" && val1 == null)) {
            return true;
        }

        if (!val1 && !val2) {
            return true;
        } else if ((!val1 && val2) || (!val2 && val1)) {
            return false;
        }

        if (attributeType.type === "term") {
            if (val1 != null && val2 != null) {
                return val1.length === val2.length && val1[0] === val2[0];
            }
        } else if (attributeType.type === "geometry") {
            return turf_booleanequal(val1, val2);
        } else if (attributeType.type === "date") {
            let casted1 = (typeof val1 === "string") ? parseInt(val1) : val1;
            let casted2 = (typeof val2 === "string") ? parseInt(val2) : val2;

            return casted1 === casted2;
        } else if (attributeType.type === "local") {
            if ((!val1.localeValues || !val2.localeValues) || val1.localeValues.length !== val2.localeValues.length) {
                return false;
            }

            let len = val1.localeValues.length;
            for (let i = 0; i < len; ++i) {
                let localeValue = val1.localeValues[i];

                let lv2 = this.getValueAtLocale(val2, localeValue.locale);
                let lv1 = localeValue.value;

                if ((lv1 === "" && lv2 == null) || (lv2 === "" && lv1 == null)) {
                    continue;
                } else if (lv1 !== lv2) {
                    return false;
                }
            }

            return true;
        } else if (attributeType.type === "_PARENT_" && val1.parents && val2.parents) {
            for (const [gotCode, data] of Object.entries(val1.parents)) {
                let parentData: { text: string, geoObject: GeoObject } = data as { text: string, geoObject: GeoObject };

                if (val2.parents[gotCode]) {
                    let parentData2: { text: string, geoObject: GeoObject } = val2.parents[gotCode];

                    if (parentData.geoObject == null && parentData2.geoObject == null) {
                        // equal, keep looping
                    } else if ((parentData.geoObject == null && parentData2.geoObject != null) || (parentData2.geoObject == null && parentData.geoObject != null)) {
                        return false;
                    } else if (parentData.geoObject.properties.code !== parentData2.geoObject.properties.code) {
                        return false;
                    }
                }
            }

            return true;
        } else if (attributeType.type === "integer") {
            let casted1 = (typeof val1 === "string") ? parseInt(val1) : val1;
            let casted2 = (typeof val2 === "string") ? parseInt(val2) : val2;

            return casted1 === casted2;
        } else if (attributeType.type === "float") {
            let casted1 = (typeof val1 === "string") ? parseFloat(val1) : val1;
            let casted2 = (typeof val2 === "string") ? parseFloat(val2) : val2;

            return casted1 === casted2;
        }

        return val1 === val2;
    }

    public static getValueAtLocale(lv: LocalizedValue, locale: string) {
        return new LocalizedValue(lv.localizedValue, lv.localeValues).getValue(locale);
    }

    static arrayMove(arr: any[], oldIndex: number, newIndex: number): void {
        if (newIndex >= arr.length) {
            let k = newIndex - arr.length + 1;
            while (k--) {
                arr.push(undefined);
            }
        }
        arr.splice(newIndex, 0, arr.splice(oldIndex, 1)[0]);
    }

}
