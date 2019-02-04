import { Injectable } from '@angular/core';
import { Subject } from 'rxjs/Subject';

import {  ManageGeoObjectTypeModalState, GeoObjectTypeModalStates } from '../data/hierarchy/hierarchy';


declare var acp: string;

@Injectable()
export class GeoObjectTypeManagementService {

    modalState: ManageGeoObjectTypeModalState;
    private modalStateChangedSource = new Subject<ManageGeoObjectTypeModalState>();
    modalStepChange = this.modalStateChangedSource.asObservable();

    constructor(  ) { }

    public getModalState(): ManageGeoObjectTypeModalState {
        return this.modalState;
    }

    public setModalState(state: ManageGeoObjectTypeModalState): void {
        this.modalState = state;
        
        this.modalStateChangedSource.next(this.modalState);
    }
}
