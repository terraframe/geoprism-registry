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

import { Component, OnInit } from "@angular/core";

import { ActivatedRoute } from "@angular/router";
import { GenericTableConfig, TableColumnSetup, TableEvent } from "@shared/model/generic-table";
import { BusinessTypeService } from "@registry/service/business-type.service";
import { BusinessType } from "@registry/model/business-type";
import { LocalizationService } from "@shared/service/localization.service";

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
