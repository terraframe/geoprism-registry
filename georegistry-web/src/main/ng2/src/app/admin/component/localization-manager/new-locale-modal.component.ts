import { Component, Input } from '@angular/core';
import { Subject } from 'rxjs/Subject';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalService } from 'ngx-bootstrap/modal';
import { LocalizationManagerService } from '../../service/localization-manager.service';
import { EventService } from '../../../shared/service/event.service';
import { AllLocaleInfo } from '../../model/localization-manager';
import { ErrorModalComponent } from '../../../shared/component/modals/error-modal.component';

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
      .then(() => {
        this.onSuccess.next(this.language);
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
    // Handle error
    if (err !== null) {
      let bsModalRef = this.modalService.show(ErrorModalComponent, { backdrop: true });
      bsModalRef.content.message = (err.error.localizedMessage || err.error.message || err.message);
    }
  }
}
