import { Component, OnInit } from "@angular/core";
import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";
import { HttpErrorResponse } from "@angular/common/http";
import { ActivatedRoute } from "@angular/router";

import { LocalizationService, AuthService } from "@shared/service";

import { ErrorHandler, ErrorModalComponent } from "@shared/component";

import { GeoRegistryConfiguration } from "@core/model/core"; import { environment } from 'src/environments/environment';

@Component({

    selector: "change-request-page",
    templateUrl: "./change-request-page.component.html",
    styleUrls: ["./change-request-page.css"]
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
