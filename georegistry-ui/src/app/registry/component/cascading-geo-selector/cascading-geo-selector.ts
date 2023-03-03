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

import { Component, Input, EventEmitter, Output, ViewChild, SimpleChanges } from "@angular/core";
import { HttpErrorResponse } from "@angular/common/http";

import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";

import { HierarchyOverTime } from "@registry/model/registry";
import { RegistryService } from "@registry/service";

import { ErrorHandler, ErrorModalComponent } from "@shared/component";

@Component({

    selector: "cascading-geo-selector",
    templateUrl: "./cascading-geo-selector.html"
})
export class CascadingGeoSelector {

    @Input() hierarchies: HierarchyOverTime[];

    @Output() valid = new EventEmitter<boolean>();

    @Input() isValid: boolean = true;
    @Input() readOnly: boolean = false;

    @ViewChild("mainForm") mainForm;

    @Input() forDate: Date = new Date();

    @Input() customEvent: boolean = false;

    @Output() onManageVersion = new EventEmitter<HierarchyOverTime>();

    dateStr: string;

    cHierarchies: any[] = [];

    parentMap: any = {};

    bsModalRef: BsModalRef;

    // eslint-disable-next-line no-useless-constructor
    constructor(private modalService: BsModalService, private registryService: RegistryService) { }

    ngOnInit(): void {
        const day = this.forDate.getUTCDate();

        this.dateStr = this.forDate.getUTCFullYear() + "-" + (this.forDate.getUTCMonth() + 1) + "-" + (day < 10 ? "0" : "") + day;

        // Truncate any hours/minutes/etc which may be part of the date
        this.forDate = new Date(Date.parse(this.dateStr));

        this.calculate();
    }

    ngOnChanges(changes: SimpleChanges) {
        if (changes["forDate"]) {
            this.calculate();
        }
    }

    calculate(): any {
        const time = this.forDate.getTime();

        this.isValid = true;

        this.cHierarchies = [];
        this.hierarchies.forEach(hierarchy => {
            const object = {};
            object["label"] = hierarchy.label;
            object["code"] = hierarchy.code;

            this.isValid = this.isValid && (this.hierarchies.length > 0);

            hierarchy.entries.forEach(pot => {
                const startDate = Date.parse(pot.startDate);
                const endDate = Date.parse(pot.endDate);

                if (time >= startDate && time <= endDate) {
                    let parents = [];

                    hierarchy.types.forEach(type => {
                        let parent: any = {
                            code: type.code,
                            label: type.label
                        }

                        if (pot.parents[type.code] != null) {
                            parent.text = pot.parents[type.code].text;
                            parent.geoObject = pot.parents[type.code].geoObject;
                        }

                        parents.push(parent);
                    });

                    object["parents"] = parents;
                }
            });

            this.cHierarchies.push(object);
        });

        this.valid.emit();
    }

    public getIsValid(): boolean {
        return true;
    }

    public getHierarchies(): any {
        return this.hierarchies;
    }

    onManageVersions(code: string): void {
        const hierarchy = this.hierarchies.find(h => h.code === code);

        if (this.customEvent) {
            this.onManageVersion.emit(hierarchy);
        } else {
/*
            this.bsModalRef = this.modalService.show(ManageParentVersionsModalComponent, {
                animated: true,
                backdrop: true,
                ignoreBackdropClick: true,
            });
            this.bsModalRef.content.init(hierarchy);
            this.bsModalRef.content.onVersionChange.subscribe(hierarchy => {
                this.calculate();
            });
            */
        }
    }

    public error(err: HttpErrorResponse): void {
        this.bsModalRef = ErrorHandler.showErrorAsDialog(err, this.modalService);
    }

}
