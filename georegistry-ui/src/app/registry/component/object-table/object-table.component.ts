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
import { ObjectOverTime, ObjectClass } from "@registry/model/object-class";
import { GenericTableComponent } from "../../../shared/component/generic-table/generic-table.component";
import { NgClass, NgIf } from "@angular/common";
import { PageContainerComponent } from "../../../shared/component/page-container/page-container.component";
import { BusinessObjectService } from "@registry/service/business-object.service";
import { ConceptClassService } from "@registry/service/concept-class.service";
import { ConceptObjectService } from "@registry/service/concept-object.service";
import { ObjectService } from "@registry/service/object.service";
import { ObjectClassService } from "@registry/service/object-class.service";
import { ObjectPanelComponent } from "./object-panel.component";

@Component({
    selector: "object-table",
    templateUrl: "./object-table.component.html",
    styles: [],
    standalone: true,
    imports: [PageContainerComponent, NgIf, NgClass, GenericTableComponent, ObjectPanelComponent]
})
export class ObjectTableComponent implements OnInit {

    message: string = null;

    objectType: string;
    oid: string;

    type: ObjectClass;

    config: GenericTableConfig = null;
    setup: TableColumnSetup = null;

    object: ObjectOverTime;

    constructor(
        private businessTypeService: BusinessTypeService,
        private conceptClassSrvice: ConceptClassService,
        private businessService: BusinessObjectService,
        private conceptService: ConceptObjectService,

        private route: ActivatedRoute) { }

    getTypeService(): ObjectClassService<any> {
        if (this.objectType === "BUSINESS_OBJECT") {
            return this.businessTypeService;
        }

        if (this.objectType === "CONCEPT_OBJECT") {
            return this.conceptClassSrvice;
        }

    }

    getService(): ObjectService {
        if (this.objectType === "BUSINESS_OBJECT") {
            return this.businessService;
        }

        if (this.objectType === "CONCEPT_OBJECT") {
            return this.conceptService;
        }
    }

    ngOnInit(): void {
        this.oid = this.route.snapshot.paramMap.get("oid");
        this.objectType = this.route.snapshot.paramMap.get("objectType");

        this.getTypeService().get(this.oid).then(type => {
            this.type = type;

            const cols = [];
            cols.push({ header: "", type: "ACTIONS", sortable: false, rowspan: 1, colspan: 1, headerType: "ATTRIBUTE" });

            this.type.attributes
                .filter(a => !a.isChangeOverTime)
                .forEach(attribute => {
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
                service: this.getService(),
                remove: false,
                view: true,
                create: false,
                label: this.type.displayLabel.localizedValue,
                sort: [{ field: "code", order: 1 }]
            };
        });
    }

    view(code: string): void {
        this.getService().get(this.type.code, code).then(object => {
            this.object = object;
        })
    }

    onClick(event: TableEvent): void {
        if (event.type === "view") {
            this.view(event.row['code']);
        }
    }

}
