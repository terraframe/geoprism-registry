import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { ErrorHandler } from '@shared/component';

import { RegistryService, IOService, SynchronizationConfigService } from '@registry/service';
import { ScheduledJob, SynchronizationConfig } from '@registry/model/registry';

@Component({
  selector: 'job',
  templateUrl: './details.component.html',
  styleUrls: ['./details.css']
})
export class SyncDetailsComponent implements OnInit {
  message: string = null;
  job: ScheduledJob;
  historyId: string = "";

  config: SynchronizationConfig = null;

  page: any = {
    count: 0,
    pageNumber: 1,
    pageSize: 10,
    results: []
  };

  constructor(private configService: SynchronizationConfigService, public service: RegistryService, private route: ActivatedRoute, public ioService: IOService) {
  }

  ngOnInit(): void {

    this.historyId = this.route.snapshot.params["oid"];

    const configOid = this.route.snapshot.paramMap.get('config');

    this.configService.get(configOid).then(config => {
      this.config = config;

      this.onPageChange(1);
    });
  }

  ngOnDestroy() {
  }

  formatAffectedRows(rows: string) {
    return rows.replace(/,/g, ", ");
  }

  formatValidationResolve(obj: any) {
    return JSON.stringify(obj);
  }

  onPageChange(pageNumber: any): void {

    this.message = null;

    this.service.getExportDetails(this.historyId, this.page.pageSize, pageNumber).then(response => {

      this.job = response;

      this.page = this.job.exportErrors;

    }).catch((err: HttpErrorResponse) => {
      this.error(err);
    });

  }

  error(err: HttpErrorResponse): void {
    this.message = ErrorHandler.getMessageFromError(err);
  }

}
