import { LocalizedValue } from "@core/model/core";
import { AttributedType, AttributeType } from "./registry";

export class BusinessType implements AttributedType {

    oid?: string;
    code: string;
    organization: string;
    organizationLabel?: string;
    displayLabel: LocalizedValue;
    description: LocalizedValue;
    attributes?: Array<AttributeType>;
    labelAttribute?: string;

}

export class BusinessTypeByOrg {

    oid: string;
    code: string;
    label: string;
    types: BusinessType[];
    write: boolean;

}

export class BusinessObject {

    code: string;
    label: string;
    data: {
        [key: string]: string | number;
    }

}
