export enum ConflictType {
    MISSING_REFERENCE = "MISSING_REFERENCE",
    TIME_RANGE = "TIME_RANGE",
    OUTSIDE_EXISTS = "OUTSIDE_EXISTS"
}


export interface ConflictMessage {
    message: string;
    severity: string;
    type: ConflictType;
}

export interface TimeRangeEntry {
    startDate: string;
    endDate: string;
    oid?: string;
    value?: any;
}
