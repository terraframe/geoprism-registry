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
import { LabeledPropertyGraphTypeService } from "@registry/service/labeled-property-graph-type.service";
import { GeoObjectType } from "@registry/model/registry";
import Utils from "@registry/utility/Utils";
import { PRESENT } from "@shared/model/date";
import { HierarchyType } from "@registry/model/hierarchy";
import { TypeaheadMatch } from "ngx-bootstrap/typeahead";

@Component({
    selector: "labeled-property-graph-type-publish-modal",
    templateUrl: "./publish-modal.component.html",
    styleUrls: ["./labeled-property-graph-type-manager.css"]
})
export class LabeledPropertyGraphTypePublishModalComponent implements OnInit {

    currentDate: Date = new Date();
    message: string = null;
    onChange: any = null;

    type: LabeledPropertyGraphType = null;

    types: GeoObjectType[] = [];
    hierarchies: HierarchyType[] = [];

    tab: string = "LIST";

    readonly: boolean = false;

    isNew: boolean = false;

    valid: boolean = true;

    gap: boolean = false;

    dataSource: Observable<any>;

    entityLabel: string = '';


    // eslint-disable-next-line no-useless-constructor
    constructor(
        private service: LabeledPropertyGraphTypeService,
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

        this.registryService.init().then(response => {
            this.types = response.types.filter(type => !type.isAbstract);
            this.hierarchies = response.hierarchies;

            if (type == null) {
                this.isNew = true;
                this.type = {
                    oid: null,
                    graphType: "single",
                    displayLabel: this.lService.create(),
                    description: this.lService.create(),
                    code: "graph_" + Math.floor(Math.random() * 999999),
                    hierarchies: [],
                    types: [],
                    strategyType: "TREE",
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
        this.service.apply(this.type).then(response => {
            if (this.onChange != null) {
                this.onChange(response);
            }
            this.bsModalRef.hide();
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    onSetType(event): void {
        const target: HTMLInputElement = (event.target as HTMLInputElement);

        const value = target.value

        if (value != null && value.length > 0) {
            this.type.types.push(value);

            target.value = '';
        }
    }

    removeType(index: number): void {
        this.type.types.splice(index, 1);
    }

    onSetHierarchy(event): void {
        const target: HTMLInputElement = (event.target as HTMLInputElement);

        const value = target.value

        if (value != null && value.length > 0) {
            this.type.hierarchies.push(value);

            target.value = '';
        }
    }

    removeHierarchy(index: number): void {
        this.type.hierarchies.splice(index, 1);
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
        this.bsModalRef.hide();
    }

    handleTab(tab: string): void {
        this.tab = tab;
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
