import { LocalizedValue } from "@shared/model/core";

export class Transition {
    oid?: string;
    sourceCode: string;
    sourceType: string;
    sourceText?: string;
    targetCode: string;
    targetType: string;
    targetText?: string;
    transitionType: string;
}

export class TransitionEvent {
    oid?: string;
    typeCode: string;
    typeLabel?: string;
    eventDate: string;
    description: LocalizedValue;
    transitions?: Transition[];
}
