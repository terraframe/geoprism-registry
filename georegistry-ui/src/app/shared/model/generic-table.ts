///
/// Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Geoprism Registry(tm).
///
/// Geoprism Registry(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Geoprism Registry(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
///

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
