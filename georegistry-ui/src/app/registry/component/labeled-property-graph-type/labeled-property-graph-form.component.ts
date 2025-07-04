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

import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from "@angular/core";
import { Observable } from "rxjs";
import { v4 as uuid } from "uuid";

import { RegistryService } from "@registry/service";
import { DateService } from "@shared/service/date.service";

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

@Component({
    selector: "labeled-property-graph-type-form",
    templateUrl: "./labeled-property-graph-form.component.html",
    styleUrls: ["./labeled-property-graph-type-manager.css"]
})
export class LabeledPropertyGraphTypeFormComponent implements OnInit, OnDestroy {

    currentDate: Date = new Date();

    types: GeoObjectType[] = [];
    hierarchies: HierarchyType[] = [];
    graphTypes: GraphType[] = [];
    organizations: Organization[] = [];

    businessTypes: BusinessType[] = [];
    edgeTypes: BusinessEdgeType[] = [];

    tab: string = "LIST";

    valid: boolean = true;

    gap: boolean = false;

    dataSource: Observable<any>;

    readonly: boolean = false;

    @Input() type: LabeledPropertyGraphType = null;
    @Input() isNew: boolean = true;
    @Input() entityLabel: string = '';
    @Input() isExport: boolean = false;

    @Output() complete = new EventEmitter<LabeledPropertyGraphType>();
    @Output() cancel = new EventEmitter<void>();

    // eslint-disable-next-line no-useless-constructor
    constructor(
        private businessService: BusinessTypeService,
        private registryService: RegistryService,
        private lService: LocalizationService,
        private dateService: DateService) {
        this.dataSource = new Observable((observer: any) => {
            this.registryService.getGeoObjectSuggestionsTypeAhead(this.entityLabel, this.type.strategyConfiguration.typeCode).then(results => {
                observer.next(results);
            });
        });


    }

    ngOnInit(): void {


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
        });

        if (this.type == null) {
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
            }
        }
    }

    ngOnDestroy(): void {
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

        if (types != null && types.length > 0) {
            let typeCodes: string[] = (codes == null || codes.length == 0) ? [] : JSON.parse(codes);
            let typeLabels = typeCodes.map(c => types.find(t => t.code == c)[labelAttribute].localizedValue);

            if (showAll) {
                return typeLabels.sort().join(", ");
            }
        }

        return text;
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

    clickTypeOption(codes: string, type: any, attribute: string) {
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
        this.complete.emit(this.type);
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

    onNewInterval(): void {
        if (this.type.intervalJson == null) {
            this.type.intervalJson = [];
        }

        this.type.intervalJson.push({
            startDate: "",
            endDate: "",
            oid: uuid()
        });
    }

    removeInterval(index: number): void {
        this.type.intervalJson.splice(index, 1);

        this.handleDateChange();
    }

    typeaheadOnSelect(e: TypeaheadMatch): void {
        this.type.strategyConfiguration.code = e.item.code;
    }


    handleDateChange(): void {
        if (this.type.graphType === "single") {
            this.valid = (this.type.validOn != null && this.type.validOn.length > 0);
        } else if (this.type.graphType === "incremental") {
            this.valid = (this.type.publishingStartDate != null && this.type.publishingStartDate.length > 0);
        } else if (this.type.graphType === "interval") {
            this.valid = this.type.intervalJson.map(interval => {
                return ((interval.startDate != null && interval.startDate.length > 0) &&
                    (interval.endDate != null && interval.endDate.length > 0) &&
                    !this.dateService.after(interval.startDate, interval.endDate));
            }).reduce((a, b) => a && b);

            // Sort the entries
            this.type.intervalJson = this.type.intervalJson.sort((a, b) => {
                const d1: Date = new Date(a.startDate);
                const d2: Date = new Date(b.startDate);

                return d1 < d2 ? 1 : -1;
            });

            // Check for overlaps
            this.type.intervalJson.forEach((element, index) => {
                if (index > 0) {
                    const future = this.type.intervalJson[index - 1];

                    if (future.startDate && future.endDate && element.startDate && element.endDate) {
                        let s1: any = new Date(future.startDate);
                        let e1: any = new Date(future.endDate);
                        let s2: any = new Date(element.startDate);
                        let e2: any = new Date(element.endDate);

                        if (Utils.dateRangeOverlaps(s1.getTime(), e1.getTime(), s2.getTime(), e2.getTime())) {
                            this.valid = false;
                        }
                    }
                }
            });

            if (this.valid) {
                // Check for gap
                this.gap = false;

                this.type.intervalJson.forEach((element, index) => {
                    if (index > 0) {
                        const future = this.type.intervalJson[index - 1];

                        if (future.startDate && element.endDate) {
                            let e1: any = new Date(element.endDate);
                            let s2: any = new Date(future.startDate);

                            if (Utils.hasGap(e1.getTime(), s2.getTime())) {
                                this.gap = true;
                            }
                        }
                    }
                });
            }
        } else {
            this.valid = true;
        }
    }

    onCancel(): void {
        this.cancel.emit();
    }

    handleTab(tab: string): void {
        this.tab = tab;
    }

}
