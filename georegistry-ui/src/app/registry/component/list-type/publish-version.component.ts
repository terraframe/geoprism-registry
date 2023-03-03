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
import { HttpErrorResponse } from "@angular/common/http";

import { ErrorHandler } from "@shared/component";
import { ListType, ListTypeEntry, ListTypeVersion, ListVersionMetadata } from "@registry/model/list-type";
import { ListTypeService } from "@registry/service/list-type.service";

@Component({
    selector: "publish-version",
    templateUrl: "./publish-version.component.html",
    styleUrls: ["./list-type-manager.css"]
})
export class PublishVersionComponent implements OnInit {

    message: string = null;

    list: ListType = null;
    entry: ListTypeEntry = null;

    metadata: ListVersionMetadata = null;

    tab: string = "LIST";

    readonly: boolean = false;

    // eslint-disable-next-line no-useless-constructor
    constructor(
        private service: ListTypeService,
        private bsModalRef: BsModalRef) { }

    ngOnInit(): void {
    }

    init(list: ListType, entry: ListTypeEntry, version?: ListTypeVersion): void {
        this.list = list;
        this.entry = entry;
        this.readonly = !list.write;

        if (version == null) {
            const working: ListTypeVersion = entry.versions[entry.versions.length - 1];

            this.metadata = {
                listMetadata: {
                    visibility: "PRIVATE",
                    master: false,
                    ...JSON.parse(JSON.stringify(working.listMetadata))
                },
                geospatialMetadata: {
                    visibility: "PRIVATE",
                    master: false,
                    ...JSON.parse(JSON.stringify(working.geospatialMetadata))
                }
            };
        } else {
            this.metadata = version;
        }
    }

    onSubmit(): void {
        if (this.metadata.oid != null) {
            this.service.applyVersion(this.metadata).then(version => {
                if (this.entry.versions != null) {
                    const index = this.entry.versions.findIndex(v => v.oid === version.oid);

                    version.collapsed = this.entry.versions[index].collapsed;

                    this.entry.versions[index] = version;
                }
                this.bsModalRef.hide();
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });
        } else {
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
