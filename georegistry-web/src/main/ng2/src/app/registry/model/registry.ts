/* eslint-disable no-use-before-define */
/* eslint-disable padded-blocks */
import { PageResult } from "@shared/model/core";
import { LocalizationService } from "@shared/service/localization.service";
import { ImportConfiguration } from "./io";
import { GovernanceStatus } from "./constants";
import Utils from "@registry/utility/Utils";
import { RegistryService } from "@registry/service";
import { LocalizedValue } from "@core/model/core";

/**
 * Provides a reusable TypeCache which can be used across the app. Types will be automatically fetched
 * from the server when the cache is constructed, however since Javascript is non-blocking they are not
 * guaranteed to be available, especially if they are in the process of being fetched from the server.
 * If you have an operation which needs to wait on the types, you may call the waitOnTypes method.
 */
export class GeoObjectTypeCache {

    private registryService: RegistryService;

    private types: GeoObjectType[];

    private refreshPromise: Promise<GeoObjectType[]>;

    public constructor(registryService: RegistryService) {
        this.registryService = registryService;

        this.refreshPromise = this.refresh();
    }

    public waitOnTypes(): Promise<GeoObjectType[]> {
        if (this.refreshPromise) {
            return this.refreshPromise;
        } else if (this.types == null) {
            return (this.refreshPromise = this.refresh());
        } else {
            return new Promise<GeoObjectType[]>((resolve, reject) => resolve(this.types));
        }
    }

    public ready(): boolean {
        return this.types != null;
    }

    public refresh(): Promise<GeoObjectType[]> {
        return this.registryService.getGeoObjectTypes(null, null).then(types => {
            this.refreshPromise = null;
            this.types = types;
            return this.types;
        }).catch(e => {
            this.types = null;
            this.refreshPromise = null;

            return this.types;
        });
    }

    public getTypeByCode(code: string): GeoObjectType {
        if (!this.types) {
            return null;
        }

        let index = this.types.findIndex(type => type.code === code);

        if (index === -1) {
            return null;
        } else {
            return this.types[index];
        }
    }

    public getTypes() {
        return this.types;
    }

}

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
        exists: boolean,
        writable?: boolean
    };
}

export interface AttributedType {
    code: string;
    attributes?: Array<AttributeType | AttributeTermType | AttributeDecimalType>;
}

export class GeoObjectType implements AttributedType {
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
    classificationType?: string;
    rootTerm?: Term;
    isValid?: boolean;
    isValidReason?: { timeConflict: boolean, existConflict: boolean, dateField: boolean };
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
    exception?: {
        message: string,
        type: string
    }
}

export class ScheduledJob extends AbstractScheduledJob {
    importedRecords: number;
    exportedRecords: number;
    configuration: ImportConfiguration;
    importErrors: PageResult<any>;
    exportErrors: PageResult<any>;
    problems: PageResult<any>;
    fileName: string;
    exception?: { type: string, message: string };
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
    fileName: string;
}
