///
/// Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Runway SDK(tm).
///
/// Runway SDK(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Runway SDK(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.
///

import { Component, OnInit } from "@angular/core";
import { HttpErrorResponse } from "@angular/common/http";

import { BsModalRef } from "ngx-bootstrap/modal";
import { Subject } from "rxjs";

import { ExternalSystem, SystemCapabilities, Organization } from "@shared/model/core";

import { LocalizationService, AuthService, ExternalSystemService } from "@shared/service";

import { ErrorHandler } from "@shared/component";

import { environment } from 'src/environments/environment';

@Component({
    selector: "external-system-modal",
    templateUrl: "./external-system-modal.component.html",
    styles: [".modal-form .check-block .chk-area { margin: 10px 0px 0 0;}"]
})
export class ExternalSystemModalComponent implements OnInit {

  message: string = null;

  connectMessage: string = null;

  editPassword: boolean = false;

  isNew: boolean = false;

  system: ExternalSystem = {
      id: "",
      type: "DHIS2ExternalSystem",
      organization: "",
      label: this.lService.create(),
      description: this.lService.create(),
      version: "2.31",
      oAuthServer: null
  };

  organizations: Organization[] = [];

  oauthEnabled: boolean = false;

  capabilities: SystemCapabilities = null;

  public onSuccess: Subject<ExternalSystem>;

  constructor(private systemService: ExternalSystemService, private authService: AuthService, public bsModalRef: BsModalRef, private lService: LocalizationService) {
  }

  ngOnInit(): void {
      this.onSuccess = new Subject();
  }

  init(organizations: Organization[], system?: ExternalSystem): void {
      this.organizations = organizations.filter(o => {
          return this.authService.isOrganizationRA(o.code);
      });

      if (system != null) {
          this.system = system;
          this.oauthEnabled = this.system.oAuthServer != null;
          this.isNew = false;
      } else {
          this.isNew = true;
          this.editPassword = true;
      }
  }

  enableOAuth(): void {
      this.oauthEnabled = true;

      if (!this.system.url.endsWith("/")) {
          this.system.url = this.system.url + "/";
      }

      this.message = null;

      this.system.oAuthServer = {
          authorizationLocation: this.system.url + "uaa/oauth/authorize",
          tokenLocation: this.system.url + "uaa/oauth/token",
          profileLocation: this.system.url + "api/me",
          clientId: "georegistry",
          secretKey: "",
          serverType: "DHIS2"
      };

      if (this.system.type === "DHIS2ExternalSystem") {
          this.getSystemCapabilities();
      }
  }

  getSystemCapabilities(): void {
      if (this.capabilities != null || this.system.type !== "DHIS2ExternalSystem" ||
        (this.system.username == null || this.system.username.length === 0) ||
        (this.isNew && (this.system.password == null || this.system.password.length === 0)) ||
        (this.system.url == null || this.system.url.length === 0)
      ) { return; }

      this.systemService.getSystemCapabilities(this.system).then(capabilities => {
          this.message = null;
          this.connectMessage = null;

          this.capabilities = capabilities;

          if (capabilities.oauth && this.oauthEnabled && this.system.oAuthServer == null) {
              this.system.oAuthServer = {
                  authorizationLocation: this.system.url + "uaa/oauth/authorize",
                  tokenLocation: this.system.url + "uaa/oauth/token",
                  profileLocation: this.system.url + "api/me",
                  clientId: "georegistry",
                  secretKey: "",
                  serverType: "DHIS2"
              };
          }
      }).catch((err: HttpErrorResponse) => {
          this.connectMessage = ErrorHandler.getMessageFromError(err);
      });
  }

  dhis2UrlKeyListener(event: any): void {
      if (event.key === "Enter") {
          this.capabilities = null;
          this.getSystemCapabilities();
      }
  }

  dhis2FocusOut(): void {
      this.capabilities = null;
      this.getSystemCapabilities();
  }

  removeOauth(): void {
      this.oauthEnabled = false;
      delete this.system.oAuthServer;
  }

  isOauthSupported(system:ExternalSystem) : boolean {
      return system.type === "DHIS2ExternalSystem" || system.type === "FhirExternalSystem";
  }

  downloadDhis2Plugin(): void {
      window.location.href = environment.apiUrl + "/api/external-system/download-dhis2-plugin";
  }

  cancel(): void {
      this.bsModalRef.hide();
  }

  onSubmit(): void {
      this.systemService.applyExternalSystem(this.system).then(data => {
          this.onSuccess.next(data);
          this.bsModalRef.hide();
      }).catch((err: HttpErrorResponse) => {
          this.error(err);
      });
  }

  public error(err: HttpErrorResponse): void {
      document.querySelector("modal-container.modal").scroll({
          top: 0,
          left: 0,
          behavior: "smooth"
      });
      this.message = ErrorHandler.getMessageFromError(err);
  }

}
