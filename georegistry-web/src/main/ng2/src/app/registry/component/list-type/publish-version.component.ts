import { Component, OnInit } from "@angular/core";
import { BsModalRef } from "ngx-bootstrap/modal";
import { HttpErrorResponse } from "@angular/common/http";

import { ErrorHandler } from "@shared/component";
import { LocalizationService } from "@shared/service";
import { ListTypeEntry, ListTypeVersion, ListVersionMetadata } from "@registry/model/list-type";
import { ListTypeService } from "@registry/service/list-type.service";

@Component({
    selector: "publish-version",
    templateUrl: "./publish-version.component.html",
    styleUrls: []
})
export class PublishVersionComponent implements OnInit {
    message: string = null;

    entry: ListTypeEntry = null;

    metadata: ListVersionMetadata = null;

    tab: string = 'LIST';

    readonly: boolean = false;

    // eslint-disable-next-line no-useless-constructor
    constructor(
        private service: ListTypeService,
        private lService: LocalizationService,
        private bsModalRef: BsModalRef) { }

    ngOnInit(): void {
    }

    init(entry: ListTypeEntry, readonly: boolean, version?: ListTypeVersion): void {

        this.entry = entry;
        this.readonly = readonly;

        if (version == null) {
            this.metadata = {
                listMetadata: {
                    description: this.lService.create(),
                    visibility: 'PRIVATE',
                    master: false,
                },
                geospatialMetadata: {
                    description: this.lService.create(),
                    visibility: 'PRIVATE',
                    master: false,
                }
            };
        }
        else {
            this.metadata = version;
        }
    }

    onSubmit(): void {
        this.service.createVersion(this.entry, this.metadata).then(version => {
            this.entry.current = version;
            this.bsModalRef.hide();
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
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
