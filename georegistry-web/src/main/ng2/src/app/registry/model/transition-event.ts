import { LocalizedValue } from "@core/model/core";

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
    typeUpdown?: string;
    typePart?: string;
    order: number;
    isNew: boolean;

}

export class TransitionEvent {

    oid?: string;
    eventId?: string;
    beforeTypeCode: string;
    afterTypeCode: string;
    beforeTypeLabel?: string;
    afterTypeLabel?: string;
    eventDate: string;
    permissions: string[];
    description: LocalizedValue;
    transitions?: Transition[];
}

export class HistoricalRow {
    eventId: string;
    eventDate: string;
    eventType: string;
    description: LocalizedValue;
    beforeType: string;
    beforeCode: string;
    beforeLabel: LocalizedValue;
    afterType: string;
    afterCode: string;
    afterLabel: string;
}

