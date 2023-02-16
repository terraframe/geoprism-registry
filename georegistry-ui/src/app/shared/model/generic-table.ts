import { PageResult } from "./core";

export interface GenericTableService {
    page(criteria: Object, pageConfig?: any): Promise<PageResult<Object>>;
}

export class GenericTableConfig {

    service: GenericTableService;
    remove: boolean;
    view?: boolean;
    edit?: boolean;
    create?: boolean;
    label: string;
    sort?: { field: string, order: number }[];
    baseZIndex?: number;
    pageSize?: number;

}

export class GenericTableColumn {

    headerType: string;
    header: string;
    rowspan: number;
    colspan: number;
    type?: string;
    sortable?: boolean;
    field?: string;
    baseUrl?: string;
    urlField?: string;
    text?: string
    filter?: boolean;
    columnType?: Function;
    onComplete?: Function;
    results?: any[];
    value?: any;
    startDate?: string;
    endDate?: string;

}

export class TableEvent {

    type: string;
    row?: Object;
    col?: GenericTableColumn;

}

export class TableColumnSetup {

    headers: GenericTableColumn[][];
    columns: GenericTableColumn[];

}