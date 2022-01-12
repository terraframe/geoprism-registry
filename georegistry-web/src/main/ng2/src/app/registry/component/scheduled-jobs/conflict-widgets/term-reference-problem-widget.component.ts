import { Component, OnInit, Input, EventEmitter, Output } from "@angular/core";
import { HttpErrorResponse } from "@angular/common/http";
import { BsModalRef, BsModalService } from "ngx-bootstrap/modal";
import { Observable } from "rxjs";
import { TypeaheadMatch } from "ngx-bootstrap/typeahead";

import { ScheduledJob } from "@registry/model/registry";
import { RegistryService, IOService } from "@registry/service";
import { DateService } from "@shared/service/date.service";

import { LocalizationService } from "@shared/service";
import { ErrorHandler } from "@shared/component";

@Component({
    selector: "term-reference-problem-widget",
    templateUrl: "./term-reference-problem-widget.component.html",
    styleUrls: []
})
export class TermReferenceProblemWidgetComponent implements OnInit {

    message: string = null;
    @Input() problem: any;
    @Input() job: ScheduledJob;
    @Output() public onProblemResolved = new EventEmitter<any>();

    termId: string = null;
    searchLabel: string;

    readonly: boolean = false;
    edit: boolean = false;

    // eslint-disable-next-line no-useless-constructor
    constructor(private service: RegistryService, private iService: IOService, private dateService: DateService,
        private lService: LocalizationService, public bsModalRef: BsModalRef, private modalService: BsModalService
    ) { }

    ngOnInit(): void {
        this.problem.parent = null;
        this.searchLabel = "";
    }

    getValidationProblemDisplayLabel(conflict: any): string {
        return conflict.type;
    }

    getTypeAheadObservable(conflict: any): Observable<any> {
        return Observable.create((observer: any) => {
            this.iService.getTermSuggestions(conflict.mdAttributeId, this.searchLabel, "20").then(results => {
                observer.next(results);
            });
        });
    }

    typeaheadOnSelect(e: TypeaheadMatch): void {
        this.termId = e.item.value;
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
            classifierId: this.termId,
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
