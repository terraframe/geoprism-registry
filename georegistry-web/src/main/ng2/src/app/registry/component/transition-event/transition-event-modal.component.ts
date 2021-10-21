import { Component, OnDestroy, OnInit } from "@angular/core";
import { BsModalRef } from "ngx-bootstrap/modal";
import { Observable, Subject } from "rxjs";
import { HttpErrorResponse } from "@angular/common/http";

import { IOService, RegistryService } from "@registry/service";
import { DateService } from "@shared/service/date.service";

import { ErrorHandler } from "@shared/component";
import { LocalizationService, AuthService } from "@shared/service";
import { Transition, TransitionEvent } from "@registry/model/transition-event";
import { TransitionEventService } from "@registry/service/transition-event.service";

@Component({
    selector: "transition-event-modal",
    templateUrl: "./transition-event-modal.component.html",
    styleUrls: []
})
export class TransitionEventModalComponent implements OnInit, OnDestroy {

    message: string = null;

    event: TransitionEvent = null;

    /*
     * Observable subject for MasterList changes.  Called when an update is successful
     */
    onEventChange: Subject<TransitionEvent>;

    /*
     * List of geo object types from the system
     */
    types: { label: string, code: string }[] = [];

    /*
     * List of geo object types from the system
     */
    readonly: boolean = false;


    constructor(private service: TransitionEventService, public rService: RegistryService, private iService: IOService, private lService: LocalizationService, public bsModalRef: BsModalRef, private authService: AuthService,
        private dateService: DateService) { }

    ngOnInit(): void {
        this.onEventChange = new Subject();

        this.iService.listGeoObjectTypes(true).then(types => {
            let myOrgTypes = [];
            for (let i = 0; i < types.length; ++i) {
                const orgCode = types[i].orgCode;
                const typeCode = types[i].superTypeCode != null ? types[i].superTypeCode : types[i].code;

                if (this.authService.isGeoObjectTypeRM(orgCode, typeCode)) {
                    myOrgTypes.push(types[i]);
                }
            }
            this.types = myOrgTypes;
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    ngOnDestroy(): void {
        this.onEventChange.unsubscribe();
    }


    init(readonly: boolean, event?: TransitionEvent): void {

        this.readonly = readonly;

        if (event != null) {
            this.event = event;
        }
        else {
            this.event = {
                beforeTypeCode: '',
                afterTypeCode: '',
                eventDate: '',
                description: this.lService.create(),
                transitions: []
            }
        }
    }

    onCreate(): void {
        this.event.transitions.push({
            sourceCode: '',
            sourceType: '',
            targetCode: '',
            targetType: '',
            transitionType: ''
        });
    }

    onChange(): void {
        this.event.transitions = [];
    }

    getTypeAheadObservable(transition: Transition, typeCode: string, property: string): Observable<any> {

        return new Observable((observer: any) => {

            this.rService.getGeoObjectSuggestions(transition[property], typeCode, null, null, null, this.event.eventDate, this.event.eventDate).then(results => {
                observer.next(results);
            });
        });
    }

    typeaheadOnSelect(selection: any, transition: Transition, property: string): void {
        if (property === 'targetText') {
            transition.targetCode = selection.item.code;
            transition.targetType = selection.item.typeCode;
            transition.targetText = selection.item.name;
        }
        else {
            transition.sourceCode = selection.item.code;
            transition.sourceType = selection.item.typeCode;
            transition.sourceText = selection.item.name;
        }
    }

    clear(transition: Transition, property: string): void {
        if (property === 'targetText') {
            transition.targetCode = '';
            transition.targetType = '';
            transition.targetText = '';
        }
        else {
            transition.sourceCode = '';
            transition.sourceType = '';
            transition.sourceText = '';
        }
    }

    remove(index: number): void {
        this.event.transitions.splice(index, 1);
    }



    onSubmit(): void {
        this.service.apply(this.event).then(response => {
            this.onEventChange.next(response);
            this.bsModalRef.hide();
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    onCancel(): void {
        this.bsModalRef.hide();
    }

    formatDate(date: string): string {
        return this.dateService.formatDateForDisplay(date);
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
