export interface MessageContainer {
    setMessage( message: string );
}

export class LocaleValue {
    locale: string;
    value: string;
}

export class LocalizedValue {
    localizedValue: string;
    localeValues: LocaleValue[];
}
