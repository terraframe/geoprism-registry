import { Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges } from "@angular/core";
import { ListTypeVersion } from "@registry/model/list-type";
import { GenericTableColumn, GenericTableConfig, GenericTableGroup } from "@shared/model/generic-table";
import { ListTypeService } from "@registry/service/list-type.service";
import { HttpErrorResponse } from "@angular/common/http";
import { LazyLoadEvent } from "primeng/api";
import { timeout } from "d3";

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
    cols: GenericTableColumn[] = null;
    groups: GenericTableGroup[][] = null;

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
        this.cols = [];
        const orderedArray = [];

        const mainGroups: GenericTableGroup[] = [];
        const subGroups: GenericTableGroup[] = [];

        this.list.attributes.forEach(group => {
            if (group.name !== "invalid") {
                mainGroups.push({
                    label: group.label,
                    colspan: group.colspan
                });

                group.columns.forEach(subgroup => {
                    subGroups.push({
                        label: subgroup.label,
                        colspan: subgroup.colspan
                    });

                    subgroup.columns.forEach(attribute => {
                        orderedArray.push(attribute);
                    });
                });
            }
        });

        this.groups = [mainGroups, subGroups];

        orderedArray.forEach(attribute => {
            if (attribute.name !== "invalid") {
                let column: GenericTableColumn = {
                    header: attribute.label,
                    field: attribute.name,
                    type: "TEXT",
                    sortable: false,
                    filter: false
                };

                if (attribute.type === "date") {
                    column.type = "DATE";
                } else if (attribute.name === "invalid" || attribute.type === "boolean") {
                    column.type = "BOOLEAN";
                } else if (attribute.type === "number") {
                    column.type = "NUMBER";
                }
                this.cols.push(column);
            }
        });
    }

    onClose(): void {
        this.close.emit();
    }

}
