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

import { Component, OnInit, Input, EventEmitter, Output } from "@angular/core";
import { BsModalRef, BsModalService } from "ngx-bootstrap/modal";

import { Observable } from "rxjs";
import { HttpErrorResponse } from "@angular/common/http";

import { TypeaheadMatch } from "ngx-bootstrap/typeahead";

import { ScheduledJob } from "@registry/model/registry";
import { RegistryService, IOService } from "@registry/service";
import { DateService } from "@shared/service/date.service";

import { ErrorHandler } from "@shared/component";
import { LocalizationService } from "@shared/service/localization.service";

@Component({
    selector: "parent-reference-problem-widget",
    templateUrl: "./parent-reference-problem-widget.component.html",
    styleUrls: []
})
export class ParentReferenceProblemWidgetComponent implements OnInit {

    message: string = null;
    @Input() problem: any;
    @Input() job: ScheduledJob;
    @Output() public onProblemResolved = new EventEmitter<any>();

    searchLabel: string;

    /*
     * Observable subject for submission.  Called when an update is successful
     */
    // onConflictAction: Subject<any>;

    readonly: boolean = false;
    edit: boolean = false;

    // eslint-disable-next-line no-useless-constructor
    constructor(private service: RegistryService, private iService: IOService, private dateService: DateService,
        private lService: LocalizationService, public bsModalRef: BsModalRef, private modalService: BsModalService
    ) { }

    ngOnInit(): void {
        // this.onConflictAction = new Subject();

        // this.searchLabel = this.problem.label;

        this.problem.parent = null;
        this.searchLabel = "";
    }

    getString(conflict: any): string {
        return JSON.stringify(conflict);
    }

    getValidationProblemDisplayLabel(conflict: any): string {
        return conflict.type;
    }

    getTypeAheadObservable(typeCode: string, conflict: any): Observable<any> {
        let parentCode = null;
        let hierarchyCode = this.job.configuration.hierarchy;

        return new Observable((observer: any) => {
            this.service.getGeoObjectSuggestions(this.searchLabel, typeCode, parentCode, null, hierarchyCode, this.job.startDate, this.job.endDate).then(results => {
                observer.next(results);
            });
        });
    }

    typeaheadOnSelect(e: TypeaheadMatch, conflict: any): void {
        this.searchLabel = '';

        this.service.getParentGeoObjects(e.item.code, conflict.typeCode, [], false, this.job.startDate).then(ancestors => {
            conflict.parent = ancestors.geoObject;

            this.searchLabel = e.item.name + ':' + e.item.code;
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    onIgnore(): void {
        let cfg = {
            resolution: "IGNORE",
            validationProblemId: this.problem.id
        };

        this.service.submitValidationResolve(cfg).then(response => {
            this.onProblemResolved.emit(this.problem);

            this.bsModalRef.hide();
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    onCreateSynonym(): void {
        let cfg = {
            validationProblemId: this.problem.id,
            resolution: "SYNONYM",
            code: this.problem.parent.properties.code,
            typeCode: this.problem.parent.properties.type,
            label: this.problem.label
        };

        this.service.submitValidationResolve(cfg).then(response => {
            this.onProblemResolved.emit(this.problem);

            this.bsModalRef.hide();
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    onCancel(): void {
        this.bsModalRef.hide();
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
