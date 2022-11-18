import { Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges } from "@angular/core";
import { ListTypeVersion } from "@registry/model/list-type";
import { GenericTableConfig, TableColumnSetup } from "@shared/model/generic-table";
import { ListTypeService } from "@registry/service/list-type.service";
import { HttpErrorResponse } from "@angular/common/http";
import { LazyLoadEvent } from "primeng/api";
import { timeout } from "d3";
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

            timeout(() => {
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
