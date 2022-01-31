import { LocalizedValue, PageResult } from "@shared/model/core";

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

export class ClassificationNode {

    classification:Classification;
    children: PageResult<ClassificationNode>;

}
