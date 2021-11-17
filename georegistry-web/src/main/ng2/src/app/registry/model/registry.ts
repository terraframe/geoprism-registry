/* eslint-disable no-use-before-define */
/* eslint-disable padded-blocks */
import { LocalizedValue } from "@shared/model/core";
import { LocalizationService } from "@shared/service";
import { ImportConfiguration } from "./io";
import { GovernanceStatus, ConflictType } from "./constants";
import Utils from "@registry/utility/Utils";

export const PRESENT: string = "5000-12-31";

export class TreeEntity {
    id: string;
    name: string;
    hasChildren: boolean;
}

export class Term {
    code: string;
    label: LocalizedValue;
    description: LocalizedValue;

    constructor(code: string, label: LocalizedValue, description: LocalizedValue) {
        this.code = code;
        this.label = label;
        this.description = description;
    }

    children: Term[] = [];

    addChild(term: Term) {
        this.children.push(term);
    }
}

export class GeoObject {
    type: string;
    geometry: any;
    properties: {
        uid: string,
        code: string,
        displayLabel: LocalizedValue,
        type: string,
        status: string[],
        sequence: string
        createDate: string,
        lastUpdateDate: string,
        invalid: boolean,
        writable?: boolean
    };
}

export class GeoObjectType {
    code: string;
    label: LocalizedValue;
    description: LocalizedValue;
    geometryType?: string;
    isLeaf: boolean;
    isGeometryEditable: boolean;
    organizationCode: string;
    attributes: Array<AttributeType | AttributeTermType | AttributeDecimalType> = [];
    relatedHierarchies?: string[];
    superTypeCode?: string;
    isAbstract?: boolean;
    isPrivate?: boolean;
    canDrag?: boolean;
    permissions?: string[];

    public static getAttribute(type: GeoObjectType, name: string) {
        let len = type.attributes.length;
        for (let i = 0; i < len; i++) {
            let attr: any = type.attributes[i];

            if (attr.code === name) {
                return attr;
            }
        }

        return null;
    }

    public static getGeoObjectTypeTermAttributeOptions(geoObjectType: GeoObjectType, termAttributeCode: string) {
        for (let i = 0; i < geoObjectType.attributes.length; i++) {
            let attr: any = geoObjectType.attributes[i];

            if (attr.type === "term" && attr.code === termAttributeCode) {
                attr = <AttributeTermType>attr;
                let attrOpts = attr.rootTerm.children;

                // only remove status of the required status type
                if (attrOpts.length > 0) {
                    if (attr.code === "status") {
                        return Utils.removeStatuses(attrOpts);
                    } else {
                        return attrOpts;
                    }
                }
            }
        }

        return null;
    }
}

export class Task {
    id: string;
    templateKey: string;
    msg: string;
    title: string;
    status: string;
    createDate: number;
    completedDate: number;
}

export class GeoObjectOverTime {

    geoObjectType: GeoObjectType;

    attributes: any;

    public constructor(geoObjectType: GeoObjectType, attributes: any) {
        this.geoObjectType = geoObjectType;
        this.attributes = attributes;
    }

    public getVotAtDate(date: Date, attrCode: string, lService: LocalizationService) {
        let retVot = { startDate: date, endDate: null, value: null };

        const time = date.getTime();

        for (let i = 0; i < this.geoObjectType.attributes.length; ++i) {
            let attr = this.geoObjectType.attributes[i];

            if (attr.code === attrCode) {
                if (attr.type === "local") {
                    retVot.value = lService.create();
                }

                if (attr.isChangeOverTime) {
                    let values = this.attributes[attr.code].values;

                    values.forEach(vot => {

                        const startDate = Date.parse(vot.startDate);
                        const endDate = Date.parse(vot.endDate);

                        if (time >= startDate && time <= endDate) {

                            if (attr.type === "local") {
                                retVot.value = JSON.parse(JSON.stringify(vot.value));
                            } else if (attr.type === "term" && vot.value != null && Array.isArray(vot.value) && vot.value.length > 0) {
                                retVot.value = vot.value[0];
                            } else {
                                retVot.value = vot.value;
                            }
                        }
                    });
                } else {
                    retVot.value = this.attributes[attr.code];
                }

                break;
            }
        }

        return retVot;
    }
}

export class ConflictMessage {
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

export class ValueOverTime implements TimeRangeEntry {
    oid: string;
    startDate: string;
    endDate: string;
    value: any;
    removable?: boolean;
}

export class AttributeOverTime {
    name: string;
    type: string;
    values: ValueOverTime[];
}

export class AttributeType {
    code: string; // On the back-end this is referred to as the AttributeType's 'name'. They are the same concept.
    type: string;
    label: LocalizedValue;
    description: LocalizedValue;
    isDefault: boolean;
    required: boolean;
    unique: boolean;
    governanceStatus: GovernanceStatus;
    isChangeOverTime?: boolean;
    precision?: number;
    scale?: number;
    isValid?: boolean;
    isValidReason?: {timeConflict: boolean, existConflict: boolean, dateField: boolean};
    isValidReasonHierarchy?: any;

    constructor(code: string, type: string, label: LocalizedValue, description: LocalizedValue, isDefault: boolean, required: boolean, unique: boolean, isChangeOverTime: boolean) {

        this.code = code;
        this.type = type;
        this.label = label;
        this.description = description;
        this.isDefault = isDefault;
        this.required = false; // Hardcoded to false because this functionality is disabled until later evaluation.
        this.unique = unique;
        this.isChangeOverTime = isChangeOverTime;
    }

}

export class AttributeTermType extends AttributeType {

    // descendants: Attribute[];

    // eslint-disable-next-line no-useless-constructor
    constructor(code: string, type: string, label: LocalizedValue, description: LocalizedValue, isDefault: boolean, required: boolean, unique: boolean, isChange: boolean) {
        super(code, type, label, description, isDefault, required, unique, isChange);
    }

    rootTerm: Term = new Term(null, null, null);

    termOptions: Term[] = [];

    setRootTerm(term: Term) {
        this.rootTerm = term;
    }
}

export class AttributeDecimalType extends AttributeType {
    constructor(code: string, type: string, label: LocalizedValue, description: LocalizedValue, isDefault: boolean, required: boolean, unique: boolean, isChange: boolean) {
        super(code, type, label, description, isDefault, required, unique, isChange);

        this.precision = 32;
        this.scale = 8;
    }
}

export class TreeNode {
    geoObject: GeoObject;
    hierarchyType: string;
}

export class ChildTreeNode extends TreeNode {
    children: ChildTreeNode[];
}

export class ParentTreeNode extends TreeNode {
    parents: ParentTreeNode[];
}

export class ManageGeoObjectTypeModalState {
    state: string;
    attribute: any;
    termOption: any;
}

export class PaginationPage {
    pageNumber: number;
    count: number;
    pageSize: number;
    results: any[];
}

export class AbstractScheduledJob {
    jobId: string;
    historyId: string;
    stage: string;
    status: string;
    author: string;
    createDate: string;
    lastUpdateDate: string;
    workProgress: number;
    workTotal: number;
    startDate: string;
    endDate: string;
}

export class ScheduledJob extends AbstractScheduledJob {
    importedRecords: number;
    exportedRecords: number;
    configuration: ImportConfiguration;
    importErrors: PaginationPage;
    exportErrors: PaginationPage;
    problems: PaginationPage;
    fileName: string;
}

export class ScheduledJobOverview extends ScheduledJob {
    stepConfig: StepConfig;
}

// export class ScheduledJobDetail extends ScheduledJob {
//     // failedRowCount: number;
//     importErrors: PaginationPage
// }

export class ImportError {
    exception: ServerException;
    object: ImportErrorObject;
    objectType: string;
    id: string;
    resolution: string;
    selected?: boolean;
}

export class ServerException {
    attributes: ServerExceptionAttribute[];
    type: string;
    message: string;
}

export class ServerExceptionAttribute {
    value: string;
    key: string;
}

export class ImportErrorObject {
    geoObject: GeoObjectOverTime;
    parents: HierarchyOverTime[];
    isNew: boolean;
}

export class StepConfig {
    steps: Step[];
}

export class Step {
    label: string;
    complete?: boolean;
    enabled?: boolean;
    status?: string;
}

export class HierarchyOverTime {
    code: string;
    label: string;
    types: {
        code: string;
        label: string;
    }[];

    entries: HierarchyOverTimeEntry[];
}

export class HierarchyOverTimeEntry implements TimeRangeEntry {
    startDate: string;
    endDate: string;
    oid: string;
    parents: { [k: string]: HierarchyOverTimeEntryParent };
    loading?: any;
    conflictType?: string;
    conflictMessage?: any[];
}

export class HierarchyOverTimeEntryParent {
    text: string;
    geoObject: GeoObject;
    goCode?: string;
}


export class SynchronizationConfig {
    oid?: string;
    type?: string;
    systemLabel?: string;
    isImport?: boolean;
    organization: string;
    system: string;
    hierarchy: string;
    label: LocalizedValue;
    configuration: any;
}

export class OrgSyncInfo {
    label: string;
    code: string;
    hierarchies: { label: string, code: string }[];
    systems: { label: string, oid: string, type: string }[];
}

export class ExportScheduledJob extends AbstractScheduledJob {
    stepConfig?: StepConfig;
}

export class ContextLayer {
    oid: string;
    displayLabel: string;
    active: boolean;
    enabled: boolean;
}

export class ContextLayerGroup {
    oid: string;
    displayLabel: string;
    contextLayers: ContextLayer[];
}
