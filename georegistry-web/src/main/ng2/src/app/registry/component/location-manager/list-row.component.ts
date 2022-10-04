import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from "@angular/core";
import { ListTypeVersion } from "@registry/model/list-type";
import { GenericTableColumn, GenericTableConfig } from "@shared/model/generic-table";
import { ListTypeService } from "@registry/service/list-type.service";
import { HttpErrorResponse } from "@angular/common/http";
import { LazyLoadEvent } from "primeng/api";

@Component({
    selector: "list-row",
    templateUrl: "./list-row.component.html",
    styleUrls: []
})
export class ListRowComponent implements OnInit, OnDestroy {

    @Input() oid: string;
    @Input() uid: string;

    @Output() error: EventEmitter<HttpErrorResponse> = new EventEmitter<HttpErrorResponse>();

    @Output() close: EventEmitter<void> = new EventEmitter<void>();

    list: ListTypeVersion = null;
    tableState: LazyLoadEvent = null;

    config: GenericTableConfig = null;
    cols: GenericTableColumn[] = null;

    // eslint-disable-next-line no-useless-constructor
    constructor(private service: ListTypeService) {
    }

    ngOnInit(): void {
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

    ngOnDestroy(): void {
    }

    refreshColumns(): void {
        this.cols = [];

        let orderedArray = [];
        let code = this.list.attributes.filter(obj => {
            return obj.name === "code";
        });
        let label = this.list.attributes.filter(obj => {
            return obj.name.includes("displayLabel");
        });

        orderedArray.push(code[0], ...label);

        if (this.list.isMember || this.list.listMetadata.visibility === "PUBLIC") {
            let customAttrs = [];
            let otherAttrs = [];
            this.list.attributes.forEach(attr => {
                if (attr.type === "input" && attr.name !== "latitude" && attr.name !== "longitude") {
                    customAttrs.push(attr);
                } else if (attr.name !== "code" && !attr.name.includes("displayLabel") && attr.name !== "latitude" && attr.name !== "longitude") {
                    otherAttrs.push(attr);
                }
            });

            orderedArray.push(...customAttrs, ...otherAttrs);
        }

        let coords = this.list.attributes.filter(obj => {
            return obj.name === "latitude" || obj.name === "longitude";
        });

        if (coords.length === 2) {
            orderedArray.push(...coords);
        }

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
