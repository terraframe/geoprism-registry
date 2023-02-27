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
import { HttpErrorResponse } from "@angular/common/http";
import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";

import { ErrorHandler } from "@shared/component";
import { LocalizationService, AuthService } from "@shared/service";

@Component({

    selector: "data-page",
    templateUrl: "./data-page.component.html",
    styleUrls: ["./data-page.css"]
})
export class DataPageComponent implements OnInit {

    content: string = "SPREADSHEET";
    pageTitle: string;
    bsModalRef: BsModalRef;
    isAdmin: boolean;
    isMaintainer: boolean;
    isContributor: boolean;

    constructor(private localizationService: LocalizationService, private modalService: BsModalService, private service: AuthService) {
        this.isAdmin = service.isAdmin();
        this.isMaintainer = this.isAdmin || service.isMaintainer();
        this.isContributor = this.isAdmin || this.isMaintainer || service.isContributer();

        this.isMaintainer ? this.renderContent("SPREADSHEET") : this.renderContent("EXPORT");
    }

    ngOnInit(): void {
    }

    renderContent(content: string): void {
        this.content = content;

        if (content === "SPREADSHEET") {
            this.pageTitle = this.localizationService.decode("spreadsheet.title");
        } else if (content === "SHAPEFILE") {
            this.pageTitle = this.localizationService.decode("shapefile.title");
        } else if (content === "EXPORT") {
            this.pageTitle = this.localizationService.decode("io.export.title");
        }
    }

    public error(err: HttpErrorResponse): void {
        this.bsModalRef = ErrorHandler.showErrorAsDialog(err, this.modalService);
    }

}
