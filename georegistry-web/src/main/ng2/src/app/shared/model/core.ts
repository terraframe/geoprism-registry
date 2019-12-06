export interface MessageContainer {
    setMessage( message: string );
}

export class LocaleValue {
    locale: string;
    value: string;
    
    constructor( locale: string, value: string ) {
        this.locale = locale;
        this.value = value;
    }
}

export class LocalizedValue {
    localizedValue: string;
    localeValues: LocaleValue[];
    
    constructor( localizedValue: string, localeValues: LocaleValue[] ) {
        this.localizedValue = localizedValue;
        this.localeValues = localeValues;
    }
}
