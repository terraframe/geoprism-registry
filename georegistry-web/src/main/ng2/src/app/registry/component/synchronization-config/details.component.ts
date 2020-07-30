import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { interval } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';

import { LocalizationService, AuthService } from '@shared/service';
import { ErrorHandler } from '@shared/component';
import { ModalTypes } from '@shared/model/modal';

import { RegistryService, IOService } from '@registry/service';
import { ScheduledJob } from '@registry/model/registry';

@Component({
  selector: 'job',
  templateUrl: './details.component.html',
  styleUrls: ['./details.css']
})
export class SyncDetailsComponent implements OnInit {
  message: string = null;
  job: ScheduledJob;
  historyId: string = "";

  page: any = {
    count: 0,
    pageNumber: 1,
    pageSize: 10,
    results: []
  };

  isAdmin: boolean;
  isMaintainer: boolean;
  isContributor: boolean;

  constructor(public service: RegistryService,
    private router: Router, private route: ActivatedRoute,
    private localizeService: LocalizationService, authService: AuthService, public ioService: IOService) {
    this.isAdmin = authService.isAdmin();
    this.isMaintainer = this.isAdmin || authService.isMaintainer();
    this.isContributor = this.isAdmin || this.isMaintainer || authService.isContributer();
  }

  ngOnInit(): void {

    this.historyId = this.route.snapshot.params["oid"];

    this.onPageChange(1);

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
