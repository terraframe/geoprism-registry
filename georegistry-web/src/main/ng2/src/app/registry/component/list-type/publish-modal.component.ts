import { Component, OnInit } from "@angular/core";
import { BsModalRef } from "ngx-bootstrap/modal";
import { Subject } from "rxjs";
import { HttpErrorResponse } from "@angular/common/http";

import { IOService } from "@registry/service";
import { DateService } from "@shared/service/date.service";

import { ErrorHandler } from "@shared/component";
import { LocalizationService } from "@shared/service";
import { ListType, ListTypeByType } from "@registry/model/list-type";
import { ListTypeService } from "@registry/service/list-type.service";

@Component({
    selector: "list-type-publish-modal",
    templateUrl: "./publish-modal.component.html",
    styleUrls: []
})
export class ListTypePublishModalComponent implements OnInit {

    currentDate: Date = new Date();
    message: string = null;
    onListTypeChange: Subject<ListType> = null;

    list: ListType = null;

    tab: string = 'LIST';


    /*
     * List of geo object types from the system
     */
    readonly: boolean = false;

    /*
     * List of geo object types from the system
     */
    edit: boolean = false;

    isNew: boolean = false;

    // eslint-disable-next-line no-useless-constructor
    constructor(
        private service: ListTypeService,
        private iService: IOService,
        private lService: LocalizationService,
        private bsModalRef: BsModalRef,
        private dateService: DateService) { }

    ngOnInit(): void {
    }

    init(listByType: ListTypeByType, onListTypeChange: Subject<ListType>, list?: ListType): void {

        this.onListTypeChange = onListTypeChange;
        this.readonly = !listByType.write;

        if (list == null) {
            this.isNew = true;
            this.list = {
                oid: null,
                listType: 'single',
                organization: '',
                typeCode: '',
                typeLabel: '',
                displayLabel: this.lService.create(),
                description: this.lService.create(),
                code: "",
                hierarchies: [],
                subtypeHierarchies: [],
                listMetadata: {
                    label: this.lService.create(),
                    description: this.lService.create(),
                    originator: '',
                    collectionDate: '',
                    process: this.lService.create(),
                    progress: this.lService.create(),
                    accessConstraints: this.lService.create(),
                    useConstraints: this.lService.create(),
                    acknowledgements: this.lService.create(),
                    disclaimer: this.lService.create(),
                    contactName: '',
                    organization: '',
                    telephoneNumber: '',
                    email: '',
                }
            };

            this.list.typeCode = listByType.typeCode;
            this.list.typeLabel = listByType.typeLabel;
            this.list.organization = listByType.orgCode;

            this.iService.getHierarchiesForType(this.list.typeCode, true).then(hierarchies => {
                this.list.hierarchies = hierarchies;
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });

            this.iService.getHierarchiesForSubtypes(this.list.typeCode, false).then(hierarchies => {
                this.list.subtypeHierarchies = hierarchies;
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });
        }
        else {
            this.list = list;
            this.isNew = false;
        }
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
        this.service.apply(this.list).then(response => {
            this.onListTypeChange.next(response);
            this.bsModalRef.hide();
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    stringify(obj: any): string {
        return JSON.stringify(obj);
    }

    onCancel(): void {
        this.bsModalRef.hide();
    }


    formatDate(date: string): string {
        return this.dateService.formatDateForDisplay(date);
    }

    handleTab(tab: string): void {
        this.tab = tab;
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
