import { Component, OnInit, Input, EventEmitter, Output } from "@angular/core";
import { HttpErrorResponse } from "@angular/common/http";
import { BsModalRef } from "ngx-bootstrap/modal";

import { Observable } from "rxjs";

import { TypeaheadMatch } from "ngx-bootstrap/typeahead";

import { ScheduledJob } from "@registry/model/registry";
import { RegistryService, IOService } from "@registry/service";
import { DateService } from "@shared/service/date.service";

import { ErrorHandler } from "@shared/component";
import { LocalizationService } from "@shared/service";

@Component({
    selector: "row-validation-problem-widget",
    templateUrl: "./row-validation-problem-widget.component.html",
    styleUrls: []
})
export class RowValidationProblemWidgetComponent implements OnInit {

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
        private lService: LocalizationService, public bsModalRef: BsModalRef
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

        return Observable.create((observer: any) => {
            this.service.getGeoObjectSuggestions(this.searchLabel, typeCode, parentCode, null, hierarchyCode, this.job.startDate, this.job.endDate).then(results => {
                observer.next(results);
            });
        });
    }

    typeaheadOnSelect(e: TypeaheadMatch, conflict: any): void {
        this.service.getParentGeoObjects(e.item.uid, conflict.typeCode, [], false, this.job.startDate).then(ancestors => {
            conflict.parent = ancestors.geoObject;
            this.searchLabel = ancestors.geoObject.properties.displayLabel.localizedValue;
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

    formatAffectedRows(rows: string) {
        return rows.replace(/,/g, ", ");
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

    formatDate(date: string): string {
        return this.dateService.formatDateForDisplay(date);
    }

    onCancel(): void {
        this.bsModalRef.hide();
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
