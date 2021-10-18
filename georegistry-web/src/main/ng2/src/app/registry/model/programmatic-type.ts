import { LocalizedValue } from "@shared/model/core";
import { AttributeDecimalType, AttributeTermType, AttributeType } from "./registry";

export class ProgrammaticType {
    oid?: string;
    code: string;
    organization: string;
    organizationLabel?: string;
    displayLabel: LocalizedValue;
    description: LocalizedValue;
    attributes?: Array<AttributeType | AttributeTermType | AttributeDecimalType>;
}

export class ProgrammaticTypeByOrg {
    oid: string;
    code: string;
    label: string;
    types: ProgrammaticType[];
}