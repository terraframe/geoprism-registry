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

import { Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges } from "@angular/core";
import { ListTypeVersion } from "@registry/model/list-type";
import { GenericTableConfig, TableColumnSetup } from "@shared/model/generic-table";
import { ListTypeService } from "@registry/service/list-type.service";
import { HttpErrorResponse } from "@angular/common/http";
import { LazyLoadEvent } from "primeng/api";
import Utils from "@registry/utility/Utils";

@Component({
    selector: "list-row",
    templateUrl: "./list-row.component.html",
    styleUrls: []
})
export class ListRowComponent implements OnInit, OnDestroy, OnChanges {

    @Input() oid: string;
    @Input() uid: string;

    @Output() error: EventEmitter<HttpErrorResponse> = new EventEmitter<HttpErrorResponse>();

    @Output() close: EventEmitter<void> = new EventEmitter<void>();

    list: ListTypeVersion = null;
    tableState: LazyLoadEvent = null;

    config: GenericTableConfig = null;
    setup: TableColumnSetup = null;

    // eslint-disable-next-line no-useless-constructor
    constructor(private service: ListTypeService) {
    }

    ngOnInit(): void {
        this.refreshVersion();
    }

    ngOnDestroy(): void {
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes["oid"] != null) {
            this.refreshVersion();
        } else if (changes["uid"] != null) {
            this.tableState = null;

            window.setTimeout(() => {
                this.tableState = {
                    filters: {
                        uid: {
                            matchMode: "equals",
                            value: this.uid
                        }
                    }
                };
            });
        }
    }

    refreshVersion(): void {
        this.list = null;

        this.tableState = {
            filters: {
                uid: {
                    matchMode: "equals",
                    value: this.uid
                }
            }
        };

        this.service.getVersion(this.oid).then(version => {
            this.list = version;
            this.refreshColumns();

            this.config = {
                service: this.service,
                remove: false,
                view: true,
                create: false,
                label: this.list.displayLabel,
                sort: [{ field: "code", order: 1 }],
                baseZIndex: 1051,
                pageSize: 10
            };
        });
    }

    refreshColumns(): void {
        this.setup = Utils.createColumns(this.list, false, true);
    }

    onClose(): void {
        this.close.emit();
    }

}
