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
    impact: string;

}

export class TransitionEvent {

    oid?: string;
    beforeTypeCode: string;
    afterTypeCode: string;
    beforeTypeLabel?: string;
    afterTypeLabel?: string;
    eventDate: string;
    description: LocalizedValue;
    transitions?: Transition[];

}
