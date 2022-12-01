import { Component, OnInit } from "@angular/core";

import { ActivatedRoute } from "@angular/router";
import { GenericTableConfig, TableColumnSetup, TableEvent } from "@shared/model/generic-table";
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
    setup: TableColumnSetup = null;

    constructor(private service: BusinessTypeService, private localizationService: LocalizationService, private route: ActivatedRoute) { }

    ngOnInit(): void {
        const oid = this.route.snapshot.paramMap.get("oid");

        this.service.get(oid).then(businessType => {
            this.businessType = businessType;

            const cols = [];

            this.businessType.attributes.forEach(attribute => {
                let type = "TEXT";
                let sortable = true;

                if (attribute.type === "integer" || attribute.type === "decimal") {
                    type = "NUMBER";
                } else if (attribute.type === "boolean") {
                    type = "BOOLEAN";
                } else if (attribute.type === "term" || attribute.type === "classification") {
                    sortable = false;
                } else if (attribute.type === "date") {
                    type = "DATE";
                }

                cols.push({ header: attribute.label.localizedValue, field: attribute.code, type: type, sortable: sortable, filter: sortable, rowspan: 1, colspan: 1, headerType: "ATTRIBUTE" });
            });

            this.setup = {
                headers: [cols],
                columns: cols
            };

            this.config = {
                service: this.service,
                remove: false,
                view: false,
                create: false,
                label: this.businessType.displayLabel.localizedValue,
                sort: [{ field: "code", order: 1 }]
            };
        });
    }

    onClick(event: TableEvent): void {
    }

}
