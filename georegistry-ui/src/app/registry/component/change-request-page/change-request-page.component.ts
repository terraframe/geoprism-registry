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
import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";
import { HttpErrorResponse } from "@angular/common/http";
import { ActivatedRoute } from "@angular/router";

import { LocalizationService, AuthService } from "@shared/service";

import { ErrorHandler, ErrorModalComponent } from "@shared/component";

import { GeoRegistryConfiguration } from "@core/model/core"; import { environment } from 'src/environments/environment';
import { SubmitChangeRequestComponent } from "../submit-change-request/submit-change-request.component";
import { NgIf } from "@angular/common";
import { RequestTableComponent } from "../crtable/request-table.component";
import { LocalizeComponent } from "../../../shared/component/localize/localize.component";
import { PageContainerComponent } from "../../../shared/component/page-container/page-container.component";

@Component({
    selector: "change-request-page",
    templateUrl: "./change-request-page.component.html",
    styleUrls: ["./change-request-page.css"],
    standalone: true,
    imports: [PageContainerComponent, LocalizeComponent, RequestTableComponent, NgIf, SubmitChangeRequestComponent]
})
export class ChangeRequestPageComponent implements OnInit {

  pageTitle: string;
  bsModalRef: BsModalRef;
  isAdmin: boolean;
  isMaintainer: boolean;
  isContributor: boolean;
  isContributorOnly: boolean;

  urlSubscriber: any;

  highlightOid: string;

  constructor(private localizationService: LocalizationService, private modalService: BsModalService, private service: AuthService, private route: ActivatedRoute) {
      this.isAdmin = service.isAdmin();
      this.isMaintainer = this.isAdmin || service.isMaintainer();
      this.isContributor = this.isAdmin || this.isMaintainer || service.isContributer();
      this.isContributorOnly = service.isContributerOnly();
  }

  ngOnInit(): void {
      this.urlSubscriber = this.route.params.subscribe(params => {
          this.highlightOid = params["oid"];
      });
  }

  ngOnDestroy(): void {
      this.urlSubscriber.unsubscribe();
  }

  public error(err: HttpErrorResponse): void {
      this.bsModalRef = ErrorHandler.showErrorAsDialog(err, this.modalService);
  }

}
