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
import { BsModalRef } from "ngx-bootstrap/modal";
import { Observable, Subject } from "rxjs";
import { HttpErrorResponse } from "@angular/common/http";
import { v4 as uuid } from "uuid";

import { IOService, RegistryService } from "@registry/service";
import { DateService } from "@shared/service/date.service";

import { ErrorHandler } from "@shared/component";
import { LocalizationService } from "@shared/service/localization.service";
import { LabeledPropertyGraphType } from "@registry/model/labeled-property-graph-type";
import { GeoObjectType, GraphType } from "@registry/model/registry";
import { Organization } from '@shared/model/core';
import Utils from "@registry/utility/Utils";
import { PRESENT } from "@shared/model/date";
import { HierarchyNode, HierarchyType } from "@registry/model/hierarchy";
import { TypeaheadMatch } from "ngx-bootstrap/typeahead";
import { BusinessEdgeType, BusinessType } from "@registry/model/business-type";
import { BusinessTypeService } from "@registry/service/business-type.service";
import { Router } from '@angular/router';

@Component({
    selector: "rdf-export-modal",
    templateUrl: "./rdf-export-modal.component.html",
    styleUrls: ["../../labeled-property-graph-type/labeled-property-graph-type-manager.css"]
})
export class RDFExportModalComponent implements OnInit {

    currentDate: Date = new Date();
    message: string = null;
    onChange: any = null;

    type: LabeledPropertyGraphType = null;

    exportGeometryType: string | null = "NO_GEOMETRIES";

    types: GeoObjectType[] = [];
    hierarchies: HierarchyType[] = [];
    graphTypes: GraphType[] = [];
    organizations: Organization[] = [];

    businessTypes: BusinessType[] = [];
    edgeTypes: BusinessEdgeType[] = [];

    tab: string = "LIST";

    readonly: boolean = false;

    isNew: boolean = false;

    valid: boolean = true;

    gap: boolean = false;

    dataSource: Observable<any>;

    entityLabel: string = '';


    // eslint-disable-next-line no-useless-constructor
    constructor(
        private service: RegistryService,
        private router: Router,
        private businessService: BusinessTypeService,
        private registryService: RegistryService,
        private lService: LocalizationService,
        private bsModalRef: BsModalRef,
        private dateService: DateService) {
        this.dataSource = new Observable((observer: any) => {
            this.registryService.getGeoObjectSuggestionsTypeAhead(this.entityLabel, this.type.strategyConfiguration.typeCode).then(results => {
                observer.next(results);
            });
        });


    }

    ngOnInit(): void {
    }

    init(onChange: any, type?: LabeledPropertyGraphType): void {

        this.onChange = onChange;

        this.businessService.getAll().then(response => {
            this.businessTypes = response;
        })

        this.businessService.getEdges().then(response => {
            this.edgeTypes = response;
        })

        this.registryService.init(false, true).then(response => {
            this.hierarchies = response.hierarchies;
            this.types = response.types;
            this.graphTypes = response.graphTypes;
            this.organizations = response.organizations;

            if (type == null) {
                this.isNew = true;
                this.type = {
                    oid: null,
                    graphType: "single",
                    displayLabel: this.lService.create(),
                    description: this.lService.create(),
                    code: "graph_" + Math.floor(Math.random() * 999999),
                    hierarchy: '',
                    strategyType: "",
                    strategyConfiguration: {
                        code: null,
                        typeCode: null
                    }

                };
            } else {
                this.type = type;
                this.isNew = false;
                this.entityLabel = this.type.strategyConfiguration.code;

                if (this.type.graphType === "interval") {
                    this.type.intervalJson.forEach(interval => {
                        interval.readonly = interval.endDate !== PRESENT ? "BOTH" : "START";
                        interval.oid = uuid();
                    });
                }
            }

        });
    }

    buildGraphTypeButtonLabel(showAll: boolean = false): string {
        let labels: string[] = [];
        let sep = "$@~";
        let agtr: string[] = (this.type.graphTypes == null || this.type.graphTypes.length == 0) ? [] : JSON.parse(this.type.graphTypes);

        for (let i = 0; i < agtr.length; ++i) {
            let typeCode = agtr[i].split(sep)[0];
            let code = agtr[i].split(sep)[1];

            labels.push(this.graphTypes.find(t => t.code == code).label.localizedValue);
        }

        if (showAll) {
            // if (labels.length == 0) return this.lService.decode("synchronization.config.none");
            return labels.sort().join(", ");
        } else {
            return this.lService.decode("lpg.assignGraphTypes");
        }

        /*
        } else if (labels == null || labels.length == 0) {
          return this.lService.decode("lpg.assignGraphTypes");
        } else if (labels.length > 2) {
          return this.lService.decode("sync.dhis2.orgUnit.multipleSelected");
        } else {
          return labels.join(", ");
        }
        */
    }

    clickGraphTypeOption($event, graphType: GraphType) {
        let agtr: string[] = (this.type.graphTypes == null || this.type.graphTypes.length == 0) ? [] : JSON.parse(this.type.graphTypes);
        let key = graphType.typeCode + "$@~" + graphType.code;

        if (agtr.indexOf(key) == -1) {
            agtr.push(key);
        }
        else {
            agtr.splice(agtr.indexOf(key), 1);
        }

        this.type.graphTypes = JSON.stringify(agtr);

        $event.stopPropagation();
    }

    buildGeoObjectTypeButtonLabel(showAll: boolean = false): string {
        return this.buildTypeButtonLabel(this.types, this.type.geoObjectTypeCodes, 'label', this.lService.decode("lpg.assignGeoObjectTypes"), showAll)
    }

    buildBusinessTypeButtonLabel(showAll: boolean = false): string {
        return this.buildTypeButtonLabel(this.businessTypes, this.type.businessTypeCodes, 'displayLabel', 'Assign Business Types', showAll)
    }

    buildEdgeTypeButtonLabel(showAll: boolean = false): string {
        return this.buildTypeButtonLabel(this.edgeTypes, this.type.businessEdgeCodes, 'label', 'Assign Business Edges', showAll)
    }

    buildTypeButtonLabel(types: any, codes: string, labelAttribute: string, text: string, showAll: boolean = false): string {
        let typeCodes: string[] = (codes == null || codes.length == 0) ? [] : JSON.parse(codes);
        let typeLabels = typeCodes.map(c => types.find(t => t.code == c)[labelAttribute].localizedValue);

        if (showAll) {
            return typeLabels.sort().join(", ");
        } else {
            return text
        }
    }

    clickGeoObjectTypeOption($event, geoObjectType: GeoObjectType) {

        this.clickTypeOption(this.type.geoObjectTypeCodes, geoObjectType, "geoObjectTypeCodes")

        $event.stopPropagation();
    }

    clickBusinessTypeOption($event, businessType: BusinessType) {

        this.clickTypeOption(this.type.businessTypeCodes, businessType, "businessTypeCodes")

        $event.stopPropagation();
    }

    clickBusinessEdgeOption($event, edgeType: BusinessEdgeType) {

        this.clickTypeOption(this.type.businessEdgeCodes, edgeType, "businessEdgeCodes")

        $event.stopPropagation();
    }

    clickTypeOption(codes:string, type: any, attribute: string) {
        let typeCodes: string[] = (codes == null || codes.length == 0) ? [] : JSON.parse(codes);

        if (typeCodes.indexOf(type.code) == -1) {
            typeCodes.push(type.code);
        }
        else {
            typeCodes.splice(typeCodes.indexOf(type.code), 1);
        }

        this.type[attribute] = JSON.stringify(typeCodes);
    }



    getIsDisabled(event): boolean {
        let elClasses = event.target.classList;
        for (let i = 0; i < elClasses.length; i++) {
            let c = elClasses[i];
            if (c === "disabled") {
                return true;
            }
        }

        return false;
    }

    onSubmit(): void {
        let agtr: string[] = (this.type.graphTypes == null || this.type.graphTypes.length == 0) ? [] : JSON.parse(this.type.graphTypes.replaceAll("$@~", "___SPLIT___"));
        let typeCodes: string[] = (this.type.geoObjectTypeCodes == null || this.type.geoObjectTypeCodes.length == 0) ? [] : JSON.parse(this.type.geoObjectTypeCodes);

        this.service.rdfExportStart(agtr, typeCodes).then(response => {
            this.bsModalRef.hide();
            this.router.navigate(["/registry/scheduled-jobs"]);
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    public strArrayContains(haystack, needle): boolean {
        if (haystack == null) return false;
        return JSON.parse(haystack).indexOf(needle) != -1;
    }


    onSetHierarchy(): void {
        const hierarchy = this.hierarchies.find(h => h.code === this.type.hierarchy);

        const types = []

        hierarchy.rootGeoObjectTypes.forEach(node => this.processNode(types, node));

        this.types = types;
    }

    processNode(types: GeoObjectType[], node: HierarchyNode): void {
        const type = this.types.find(t => t.code == node.geoObjectType);

        types.push(type);

        node.children.forEach(node => this.processNode(types, node));
    }

    typeaheadOnSelect(e: TypeaheadMatch): void {
        this.type.strategyConfiguration.code = e.item.code;
    }

    onCancel(): void {
        this.bsModalRef.hide();
    }

    handleTab(tab: string): void {
        this.tab = tab;
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
