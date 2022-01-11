import { Component, OnInit, Input, Output, EventEmitter, OnDestroy } from "@angular/core";
import { LazyLoadEvent } from "primeng/api";

import { Subject } from "rxjs";
import { GenericTableColumn, GenericTableConfig, TableEvent } from "@shared/model/generic-table";
import { PageResult } from "@shared/model/core";
import { LocalizationService } from "@shared/service";

@Component({
    selector: "generic-table",
    templateUrl: "./generic-table.component.html",
    styleUrls: ["./generic-table.css"]
})
export class GenericTableComponent implements OnInit, OnDestroy {

    page: PageResult<Object> = {
        resultSet: [],
        count: 0,
        pageNumber: 1,
        pageSize: 10
    };

    @Input() cols: GenericTableColumn[] = [];

    @Input() pageConfig: any = null;

    @Input() config: GenericTableConfig;

    @Input() refresh: Subject<void>;

    @Output() click = new EventEmitter<TableEvent>();

    loading: boolean = true;

    booleanOptions: any = [];

    event: LazyLoadEvent = null;

    constructor(private localizationService: LocalizationService) {
        this.booleanOptions = [
            { label: "", value: null },
            { value: true, label: this.localizationService.decode("change.request.boolean.option.true") },
            { value: false, label: this.localizationService.decode("change.request.boolean.option.false") }
        ];
    }

    ngOnInit(): void {
        if (this.refresh != null) {
            this.refresh.subscribe(() => {
                if (this.event != null) {
                    this.onPageChange(this.event);
                }
            });
        }
    }

    ngOnDestroy(): void {
        if (this.refresh != null) {
            this.refresh.unsubscribe();
        }
    }

    onPageChange(event: LazyLoadEvent): void {
        this.loading = true;
        this.event = event;

        setTimeout(() => {
            this.config.service.page(event, this.pageConfig).then(page => {
                this.page = page;
            }).finally(() => {
                this.loading = false;
            });
        }, 1000);
    }

    onClick(type: string, row: Object, col: GenericTableColumn): void {
        this.click.emit({
            type: type,
            row: row,
            col: col
        });
    }

    getColumnType(row: Object, col: GenericTableColumn): string {
        if (col.columnType != null) {
            return col.columnType(row);
        }

        return col.type;
    }

}
