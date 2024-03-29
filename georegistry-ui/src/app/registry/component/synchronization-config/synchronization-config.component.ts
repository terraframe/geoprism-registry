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
import { ActivatedRoute } from "@angular/router";
import { HttpErrorResponse } from "@angular/common/http";
import { webSocket, WebSocketSubject } from "rxjs/webSocket";

import { PageResult } from "@shared/model/core";
import { LocalizationService } from "@shared/service/localization.service";

import { SynchronizationConfig, ExportScheduledJob } from "@registry/model/registry";
import { SynchronizationConfigService } from "@registry/service";
import { ErrorHandler } from "@shared/component/error-handler/error-handler";
import { WebSockets } from "@shared/component/web-sockets/web-sockets";
import { Subscription } from "rxjs";

import { environment } from 'src/environments/environment';

@Component({
    selector: "synchronization-config",
    templateUrl: "./synchronization-config.component.html",
    styleUrls: []
})
export class SynchronizationConfigComponent implements OnInit {

  message: string = null;

  config: SynchronizationConfig = null;

  page: PageResult<ExportScheduledJob> = {
      pageSize: 10,
      pageNumber: 1,
      count: 1,
      resultSet: []
  };

  notifier: WebSocketSubject<{ type: string, content: any }>;
  subscription: Subscription = null;

  constructor(private service: SynchronizationConfigService, private lService: LocalizationService, private route: ActivatedRoute) { }

  ngOnInit(): void {
      const oid = this.route.snapshot.paramMap.get("oid");

      this.service.get(oid).then(config => {
          this.config = config;
          this.onPageChange(1);
      });

      let baseUrl = WebSockets.buildBaseUrl();

      this.notifier = webSocket(baseUrl + "/websocket/notify");
      this.subscription = this.notifier.subscribe(message => {
          if (message.type === "DATA_EXPORT_JOB_CHANGE") {
              this.onPageChange(this.page.pageNumber);
          }
      });
  }

  ngOnDestroy() {
      if (this.subscription != null) {
          this.subscription.unsubscribe();
      }

      if (this.notifier != null) {
          this.notifier.complete();
      }
  }

  onRun(): void {
      this.message = null;

      this.service.run(this.config.oid).then(() => {
      // Refresh the page
          this.onPageChange(this.page.pageNumber);
      }).catch((err: HttpErrorResponse) => {
          this.error(err);
      });
  }

  onGenerateFile(): void {
      window.open(environment.apiUrl + "/api/synchronization-config/generate-file?oid=" + encodeURIComponent(this.config.oid));
  }

  onPageChange(pageNumber: number): void {
      if (this.config != null) {
          this.message = null;

          this.service.getJobs(this.config.oid, pageNumber, this.page.pageSize).then(response => {
              this.formatStepConfig(response);
              this.page = response;
          }).catch((err: HttpErrorResponse) => {
              this.error(err);
          });
      }
  }

  formatJobStatus(job: ExportScheduledJob): string {
      if (job.status === "FEEDBACK") {
          return this.lService.decode("etl.JobStatus.FEEDBACK");
      } else if (job.status === "RUNNING" || job.status === "NEW") {
          return this.lService.decode("etl.JobStatus.RUNNING");
      } else if (job.status === "QUEUED") {
          return this.lService.decode("etl.JobStatus.QUEUED");
      } else if (job.status === "SUCCESS") {
          return this.lService.decode("etl.JobStatus.SUCCESS");
      } else if (job.status === "CANCELED") {
          return this.lService.decode("etl.JobStatus.CANCELED");
      } else if (job.status === "FAILURE") {
          return this.lService.decode("etl.JobStatus.ERROR");
      } else if (job.status === "WARNING") {
          return this.lService.decode("etl.JobStatus.WARNING");
      } else {
          return this.lService.decode("etl.JobStatus.RUNNING");
      }
  }

  formatStepConfig(page: PageResult<ExportScheduledJob>): void {
      page.resultSet.forEach((job: ExportScheduledJob) => {
          const steps = [
              {
                  label: this.lService.decode("synchronization.step.Queued"),
                  status: job.stage === "NEW" ? this.getJobStatus(job) : this.getCompletedStatus(job.stage, "NEW")
              },

              {
                  label: this.lService.decode("synchronization.step.Connecting"),
                  status: job.stage === "CONNECTING" || job.stage === "CONNECTION_FAILED" ? this.getJobStatus(job) : this.getCompletedStatus(job.stage, "CONNECTION_FAILED")
              }
          ];

          const stepLabel = this.config.isImport ? "Importing" : this.lService.decode("synchronization.step.DatabaseExport");

          steps.push({
              label: stepLabel,
              status: job.stage === "EXPORT" || job.stage === "EXPORT_RESOLVE" || job.stage === "RESUME_EXPORT" ? this.getJobStatus(job) : ""
          });

          job.stepConfig = {
              steps: steps
          };
      });
  }

  getCompletedStatus(jobStage: string, targetStage: string): string {
      let order = ["CONNECTING", "CONNECTION_FAILED", "EXPORT", "EXPORT_RESOLVE", "RESUME_EXPORT"];

      let jobPos = order.indexOf(jobStage);
      let targetPos = order.indexOf(targetStage);

      if (targetPos < jobPos) {
          return "COMPLETE";
      } else {
          return "";
      }
  }

  getJobStatus(job: ExportScheduledJob): string {
      if (job.status === "QUEUED" || job.status === "RUNNING") {
          return "WORKING";
      } else if (job.status === "FEEDBACK" || job.status === "FAILURE") {
          return "STUCK";
      }

      return "";
  }

  error(err: HttpErrorResponse): void {
      this.message = ErrorHandler.getMessageFromError(err);
  }

}
