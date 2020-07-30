import { Component, Input } from '@angular/core';
import { Subject } from 'rxjs';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalService } from 'ngx-bootstrap/modal';
import { LocalizationManagerService } from '@admin/service/localization-manager.service';
import { AllLocaleInfo } from '@admin/model/localization-manager';

import { EventService } from '@shared/service';
import { ErrorHandler, ErrorModalComponent } from '@shared/component';

@Component({
  selector: 'new-locale-modal',
  templateUrl: './new-locale-modal.component.html',
  styleUrls: []
})
export class NewLocaleModalComponent {

  allLocaleInfo: AllLocaleInfo;
  language: string;
  country: string;
  variant: string;

  public onSuccess: Subject<any>;

  constructor(public bsModalRef: BsModalRef,
    private localizationManagerService: LocalizationManagerService,
    private eventService: EventService,
    private modalService: BsModalService
  ) { }

  ngOnInit(): void {
    this.allLocaleInfo = new AllLocaleInfo();

    this.localizationManagerService.getNewLocaleInfo()
      .then(allLocaleInfoIN => {
        this.allLocaleInfo = allLocaleInfoIN;
        this.eventService.complete();
      }).catch((err: HttpErrorResponse) => {
        console.log(err);

        this.bsModalRef.hide();
        this.eventService.complete();
        this.error(err);
      });

    this.onSuccess = new Subject();
  }

  submit(): void {
    this.eventService.start();

    this.localizationManagerService.installLocale(this.language, this.country, this.variant)
      .then((response:{locale:string}) => {
        this.onSuccess.next(response.locale);

        this.eventService.complete();
        this.bsModalRef.hide();
      }).catch((err: HttpErrorResponse) => {
        console.log(err);

        this.bsModalRef.hide();
        this.eventService.complete();
        this.error(err);
      });
  }

  cancel(): void {
    this.bsModalRef.hide();


  }

  public error(err: HttpErrorResponse): void {
      let bsModalRef = this.modalService.show(ErrorModalComponent, { backdrop: true });
      bsModalRef.content.message = ErrorHandler.getMessageFromError(err);
  }
}
