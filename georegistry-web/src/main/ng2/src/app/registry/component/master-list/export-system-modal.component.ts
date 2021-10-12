import { Component   } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs';

import { ExternalSystemService } from '@shared/service';
import { ExternalSystem } from '@shared/model/core';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'export-system-modal',
  templateUrl: './export-system-modal.component.html',
  styleUrls: []
})
export class ExportSystemModalComponent {

  systems: ExternalSystem[] = [];

  system: string = null;

  /*
   * Called on confirm
   */
  public onSystemSet: Subject<string>;

  constructor(public bsModalRef: BsModalRef, private externalSystemService: ExternalSystemService) { }

  ngOnInit(): void {
    this.onSystemSet = new Subject();

    this.externalSystemService.getExternalSystems(1, 1000).then(systems => {
      this.systems = systems.resultSet.filter(sys => sys.type === 'FhirExternalSystem');
    }).catch((err: HttpErrorResponse) => {
    });

  }

  confirm(): void {
    this.bsModalRef.hide();
    this.onSystemSet.next(this.system);
  }
}
