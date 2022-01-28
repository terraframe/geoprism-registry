import { LocalizedValue } from "@shared/model/core";

export class ClassificationType {

    oid?: string;
    code: string;
    displayLabel: LocalizedValue;
    description: LocalizedValue;

}

export class Classification {

    code: string;
    displayLabel: LocalizedValue;
    description: LocalizedValue;

}
