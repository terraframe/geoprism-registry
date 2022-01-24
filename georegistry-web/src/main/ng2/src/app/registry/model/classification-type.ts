import { LocalizedValue } from "@shared/model/core";

export class ClassificationType {

    oid?: string;
    code: string;
    type?: string;
    displayLabel: LocalizedValue;
    description: LocalizedValue;

}

export class Classification {

    code: string;

}
