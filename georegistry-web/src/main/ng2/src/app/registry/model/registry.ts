/* eslint-disable no-use-before-define */
/* eslint-disable padded-blocks */
import { LocalizedValue } from "@shared/model/core";
import { LocalizationService } from "@shared/service";
import { ImportConfiguration } from "./io";
import { GovernanceStatus, ConflictType } from "./constants";

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

export class MasterList {
    oid: string;
    typeCode: string;
    typeLabel?: LocalizedValue;
    displayLabel: LocalizedValue;
    code: string;
    representativityDate: Date;
    publishingStartDate?: Date;
    publishDate: Date;
    listAbstract: string;
    process: string;
    progress: string;
    accessConstraints: string;
    useConstraints: string;
    acknowledgements: string;
    disclaimer: string;
    contactName: string;
    organization: string;
    telephoneNumber: string;
    email: string;
    hierarchies: { label: string, code: string, parents: { label: string, code: string }[] }[];
    leaf: boolean;
    frequency: string;
    isMaster: boolean;
    visibility: string;
    write?: boolean;
    read?: boolean;
    exploratory?: boolean;
    versions?: MasterListVersion[];
    subtypes?: { label:string, code: string }[];
}

export class MasterListVersion {
    displayLabel: string;
    oid: string;
    typeCode: string;
    orgCode: string;
    masterlist: string;
    forDate: string;
    createDate: string;
    publishDate: string;
    attributes: any[];
    isGeometryEditable: boolean;
    locales?: string[];
    shapefile?: boolean;
    isAbstract?: boolean;
    superTypeCode?: string;
    refreshProgress?: any;
    subtypes?: { label:string, code: string }[];
}

export enum SummaryKey {
  NEW = "NEW",
  UNMODIFIED = "UNMODIFIED",
  DELETE = "DELETE",
  UPDATE = "UPDATE",
  TIME_CHANGE = "TIME_CHANGE",
  VALUE_CHANGE = "VALUE_CHANGE",
}

export interface TimeRangeEntry {
  startDate: string;
  endDate: string;
  conflictMessage?: any[];  
  summaryKeyData?: SummaryKey;
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

export class HierarchyOverTimeEntry implements TimeRangeEntry  {
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

export class MasterListByOrg {
    oid: string;
    label: string;
    write: boolean;
    lists: {
        label: string,
        oid: string,
        createDate: string,
        lastUpdateDate: string,
        isMaster: boolean,
        write: boolean,
        read: boolean,
        visibility: string
    }[];
}

export class SynchronizationConfig {
    oid?: string;
    type?: string;
    systemLabel?: string;
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

export class VersionOverTimeLayer {
  view: any; // TODO : We might be able to get rid of this. It shouldn't be used anywhere other than the manage versions component
  oid: string;
  startDate: string;
  endDate: string;
  geojson: any;
  editing: boolean;
}
