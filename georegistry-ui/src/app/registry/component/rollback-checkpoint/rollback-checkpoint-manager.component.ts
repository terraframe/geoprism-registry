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

import { Component, OnDestroy, OnInit } from "@angular/core";
import { BsModalService } from "ngx-bootstrap/modal";
import { HttpErrorResponse } from "@angular/common/http";

import {
  ErrorHandler,
  ConfirmModalComponent,
  ProgressBarComponent,
} from "@shared/component";
import { LocalizationService } from "@shared/service/localization.service";
import { RollbackCheckpointService } from "@registry/service/rollback-checkpoint.service";
import { PageResult } from "@shared/model/core";
import { RollbackCheckpoint } from "@registry/model/rollback-checkpoint";
import { NgxPaginationModule } from "ngx-pagination";
import { RouterLink } from "@angular/router";
import { NgIf, NgFor } from "@angular/common";
import { LocalizeComponent } from "../../../shared/component/localize/localize.component";
import { PageContainerComponent } from "../../../shared/component/page-container/page-container.component";
import { ModalTypes } from "@shared/model/modal";
import { webSocket, WebSocketSubject } from "rxjs/webSocket";
import { Subscription } from "rxjs";
import { WebSockets } from "@shared/component/web-sockets/web-sockets";
import { Progress } from "@shared/model/progress";
import { ProgressService } from "@shared/service";

@Component({
  selector: "rollback-checkpoint-manager",
  templateUrl: "./rollback-checkpoint-manager.component.html",
  styleUrls: ["./rollback-checkpoint-manager.css"],
  standalone: true,
  imports: [
    PageContainerComponent,
    LocalizeComponent,
    NgIf,
    NgFor,
    RouterLink,
    NgxPaginationModule,
    ProgressBarComponent,
  ],
})
export class RollbackCheckpointManagerComponent implements OnInit, OnDestroy {
  message: string | null = null;

  page: PageResult<RollbackCheckpoint> = {
    count: 0,
    pageNumber: 1,
    pageSize: 20,
    resultSet: [],
  };

  progressNotifier: WebSocketSubject<any> | null = null;
  progressSubscription: Subscription | null = null;
  inProgress: boolean = false;

  // eslint-disable-next-line no-useless-constructor
  constructor(
    private service: RollbackCheckpointService,
    private localizeService: LocalizationService,
    private modalService: BsModalService,
    private pService: ProgressService,
  ) {}

  ngOnInit(): void {
    let baseUrl = WebSockets.buildBaseUrl();

    this.progressNotifier = webSocket(baseUrl + "/websocket/progress/rollback");

    this.progressSubscription = this.progressNotifier.subscribe((message) => {
      if (message.content != null) {
        this.handleProgressChange(message.content);
      } else {
        this.handleProgressChange(message);
      }
    });

    this.onPageChange(1);
  }

  ngOnDestroy(): void {
    if (this.progressSubscription != null) {
      this.progressSubscription.unsubscribe();
    }

    if (this.progressNotifier != null) {
      this.progressNotifier.unsubscribe();
    }
  }

  onRollback(checkpoint: RollbackCheckpoint): void {
    const bsModalRef = this.modalService.show(ConfirmModalComponent, {
      animated: false,
      backdrop: true,
      ignoreBackdropClick: true,
    });
    bsModalRef.content!.message = this.localizeService
      .decode("modal.confirm.rollback")
      .replaceAll("{filename}", checkpoint.filename);
    bsModalRef.content!.submitText = this.localizeService.decode(
      "modal.button.rollback",
    );
    bsModalRef.content!.type = ModalTypes.danger;

    bsModalRef.content!.onConfirm.subscribe(() => {
      this.service.rollback(checkpoint.oid).catch((err: HttpErrorResponse) => {
        this.error(err);
      });
    });
  }

  onPageChange(pageNumber: number): void {
    this.service
      .getPage(pageNumber, 20)
      .then((page) => {
        this.page = page;
      })
      .catch((err: HttpErrorResponse) => {
        this.error(err);
      });
  }

  handleProgressChange(progress: Progress): void {
    this.inProgress = progress.current < progress.total;

    this.pService.progress(progress);

    if (!this.inProgress) {
      this.onPageChange(1);
    }
  }

  error(err: HttpErrorResponse): void {
    this.message = ErrorHandler.getMessageFromError(err);
  }
}
