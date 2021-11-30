import { Component, OnInit } from "@angular/core";
import { BsModalRef } from "ngx-bootstrap/modal";
import { HttpErrorResponse } from "@angular/common/http";

import { ErrorHandler } from "@shared/component";
import { LocalizationService } from "@shared/service";
import { ListType, ListTypeEntry, ListTypeVersion, ListVersionMetadata } from "@registry/model/list-type";
import { ListTypeService } from "@registry/service/list-type.service";

@Component({
    selector: "publish-version",
    templateUrl: "./publish-version.component.html",
    styleUrls: ["./list-type-manager.css"]
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

    init(list: ListType, entry: ListTypeEntry, version?: ListTypeVersion): void {

        this.entry = entry;
        this.readonly = !list.write;

        if (version == null) {
            this.metadata = {
                listMetadata: {
                    visibility: 'PRIVATE',
                    master: false,
                    ...JSON.parse(JSON.stringify(list.listMetadata)),
                },
                geospatialMetadata: {
                    visibility: 'PRIVATE',
                    master: false,
                    ...JSON.parse(JSON.stringify(list.geospatialMetadata)),
                }
            };
        }
        else {
            this.metadata = version;

            console.log(this.metadata);
        }
    }

    onSubmit(): void {
        if (this.metadata.oid != null) {
            this.service.applyVersion(this.metadata).then(version => {
                if (this.entry.versions != null) {
                    const index = this.entry.versions.findIndex(v => v.oid === version.oid);

                    this.entry.versions[index] = version;
                }
                this.bsModalRef.hide();
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });
        }
        else {
            this.service.createVersion(this.entry, this.metadata).then(version => {
                this.entry.versions.unshift(version);
                this.bsModalRef.hide();
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });
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
