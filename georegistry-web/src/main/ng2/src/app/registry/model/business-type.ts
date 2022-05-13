import { LocalizedValue } from "@shared/model/core";
import { AttributeDecimalType, AttributedType, AttributeTermType, AttributeType } from "./registry";

export class BusinessType implements AttributedType {

    oid?: string;
    code: string;
    organization: string;
    organizationLabel?: string;
    displayLabel: LocalizedValue;
    description: LocalizedValue;
    attributes?: Array<AttributeType | AttributeTermType | AttributeDecimalType>;
    labelAttribute?: string;

}

export class BusinessTypeByOrg {

    oid: string;
    code: string;
    label: string;
    types: BusinessType[];

}

export class BusinessObject {

    code: string;
    label: string;
    data: {
        [key: string]: string | number;
    }

}
