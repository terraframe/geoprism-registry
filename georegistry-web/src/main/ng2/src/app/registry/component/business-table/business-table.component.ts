import { Component, OnInit } from "@angular/core";

import { ActivatedRoute } from "@angular/router";
import { GenericTableColumn, GenericTableConfig, TableEvent } from "@shared/model/generic-table";
import { BusinessTypeService } from "@registry/service/business-type.service";
import { BusinessType } from "@registry/model/business-type";
import { LocalizationService } from "@shared/service";

@Component({
    selector: "business-table",
    templateUrl: "./business-table.component.html",
    styles: []
})
export class BusinessTableComponent implements OnInit {

    message: string = null;

    businessType: BusinessType;

    config: GenericTableConfig = null;
    cols: GenericTableColumn[] = [];

    constructor(private service: BusinessTypeService, private localizationService: LocalizationService, private route: ActivatedRoute) { }

    ngOnInit(): void {
        const oid = this.route.snapshot.paramMap.get("oid");

        this.service.get(oid).then(businessType => {
            this.businessType = businessType;

            this.cols = [];

            this.businessType.attributes.forEach(attribute => {
                let type = "TEXT";
                let sortable = true;

                if (attribute.type === "integer" || attribute.type === "decimal") {
                    type = "NUMBER";
                } else if (attribute.type === "boolean") {
                    type = "BOOLEAN";
                } else if (attribute.type === "term") {
                    sortable = false;
                } else if (attribute.type === "date") {
                    type = "DATE";
                }

                this.cols.push({ header: attribute.label.localizedValue, field: attribute.code, type: type, sortable: sortable, filter: sortable });
            });
            this.cols.push({
                header: this.localizationService.decode("dropdown.select.geoobject.label"),
                field: "geoObject",
                type: "TEXT",
                sortable: true
            });

            // this.cols.push({ header: "", type: "ACTIONS", sortable: false });

            this.config = {
                service: this.service,
                remove: false,
                view: false,
                create: false,
                label: this.businessType.displayLabel.localizedValue,
                sort: { field: "code", order: 1 }
            };
        });
    }

    onClick(event: TableEvent): void {
    }

}
