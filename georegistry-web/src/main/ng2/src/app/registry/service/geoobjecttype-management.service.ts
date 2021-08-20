import { Injectable } from "@angular/core";
import { Subject } from "rxjs";

import { ManageGeoObjectTypeModalState } from "@registry/model/registry";

@Injectable()
export class GeoObjectTypeManagementService {

    modalState: ManageGeoObjectTypeModalState;
    private modalStateChangedSource = new Subject<ManageGeoObjectTypeModalState>();
    modalStepChange = this.modalStateChangedSource.asObservable();

    // eslint-disable-next-line no-useless-constructor
    constructor() { }

    public getModalState(): ManageGeoObjectTypeModalState {
        return this.modalState;
    }

    public setModalState(state: ManageGeoObjectTypeModalState): void {
        this.modalState = state;

        this.modalStateChangedSource.next(this.modalState);
    }
}
