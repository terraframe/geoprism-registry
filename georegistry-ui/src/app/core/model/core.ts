///
/// Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Geoprism Registry(tm).
///
/// Geoprism Registry(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Geoprism Registry(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
///

export class LocaleValue {

    locale: string;
    value: string;

    constructor(locale: string, value: string) {
        this.locale = locale;
        this.value = value;
    }

}

export class AlternateId {
    type: string;
    id: string;
    
    public static getKey(id: AlternateId) {
        if (id instanceof ExternalId) {
            return id.id + "," + id.externalSystemId;
        }
        
        return id.type + "," + id.id;
    }
}

export class ExternalId extends AlternateId {
    externalSystemId: string;
    externalSystemLabel: string;
}

export class LocalizedValue {

    localizedValue: string;
    localeValues: LocaleValue[];

    constructor(localizedValue: string, localeValues: LocaleValue[]) {
        this.localizedValue = localizedValue;
        this.localeValues = localeValues;
    }

    public getValue(localeToString: string): string {
        let len = this.localeValues.length;

        for (let i = 0; i < len; ++i) {
            let lv = this.localeValues[i];

            if (lv.locale === localeToString) {
                return lv.value;
            }
        }

        return this.localizedValue;
    }

    /*
     * Populates lv1 with all values contained in lv2
     */
    public static populate(lv1: LocalizedValue, lv2: LocalizedValue) {
        if (lv1 == null || lv2 == null) {
            return;
        }

        lv1.localizedValue = lv2.localizedValue;

        if (lv2.localeValues != null) {
            lv2.localeValues.forEach(lv2lv => {
                if (lv1.localeValues) {
                    let found = false;

                    lv1.localeValues.forEach(lv1lv => {
                        if (!found && lv1lv.locale === lv2lv.locale) {
                            lv1lv.value = lv2lv.value;
                            found = true;
                        }
                    });

                    if (!found) {
                        lv1.localeValues.push(JSON.parse(JSON.stringify(lv2lv)));
                    }
                } else {
                    lv1.localeValues = lv2.localeValues;
                }
            });
        }
    }

}


export class LocaleView {

    label: LocalizedValue;
    toString: string;
    tag: string;
    isDefaultLocale: boolean;
    language: { label: string, code: string };
    country: { label: string, code: string };
    variant: { label: string, code: string };

}

export class GeoRegistryConfiguration {

    contextPath: string;
    locale: string;
    locales: LocaleView[];

    searchEnabled: boolean;
    graphVisualizerEnabled: boolean;
    enableBusinessData: boolean;
    enableLabeledPropertyGraph: boolean;
    defaultMapBounds: [[number]];
    localization: any;
    mapboxAccessToken: string;
    googleanalyticstoken: string;
    customFont: string;

}
