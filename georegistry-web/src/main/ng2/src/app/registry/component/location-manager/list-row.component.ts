import { Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges } from "@angular/core";
import { ListColumn, ListTypeVersion } from "@registry/model/list-type";
import { GenericTableColumn, GenericTableConfig, TableColumnSetup } from "@shared/model/generic-table";
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
        const columns = [];

        const mainGroups: GenericTableColumn[] = [];
        const subGroups: GenericTableColumn[] = [];
        const orderedArray = [];

        this.list.attributes.forEach(group => {
            if (group.name !== "invalid") {
                mainGroups.push({
                    header: group.label,
                    colspan: group.colspan,
                    rowspan: group.rowspan,
                    headerType: "GROUP"
                });

                group.columns.forEach(subgroup => {
                    if (subgroup.columns != null) {
                        subGroups.push({
                            header: subgroup.label,
                            colspan: subgroup.colspan,
                            rowspan: subgroup.rowspan,
                            headerType: "GROUP"
                        });

                        subgroup.columns.forEach(attribute => {
                            if (attribute.name !== "invalid") {
                                const column = this.createColumn(attribute);

                                orderedArray.push(column);
                                columns.push(column);
                            }
                        });
                    } else {
                        if (subgroup.name !== "invalid") {
                            const column = this.createColumn(subgroup);

                            subGroups.push(column);
                            columns.push(column);
                        }
                    }
                });
            }
        });

        this.setup = {
            headers: [mainGroups, subGroups, orderedArray],
            columns: columns
        };
    }

    createColumn(attribute: ListColumn): GenericTableColumn {
        let column: GenericTableColumn = {
            headerType: "ATTRIBUTE",
            header: attribute.label,
            field: attribute.name,
            type: "TEXT",
            sortable: false,
            filter: false,
            rowspan: attribute.rowspan,
            colspan: attribute.colspan
        };

        if (attribute.type === "date") {
            column.type = "DATE";
        } else if (attribute.name === "invalid" || attribute.type === "boolean") {
            column.type = "BOOLEAN";
        } else if (attribute.type === "number") {
            column.type = "NUMBER";
        }

        return column;
    }

    onClose(): void {
        this.close.emit();
    }

}
