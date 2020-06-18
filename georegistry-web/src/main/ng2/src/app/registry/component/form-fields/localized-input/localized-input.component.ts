import { Component, Input, Output, EventEmitter } from '@angular/core';
import { LocalizedValue } from '../../../../shared/model/core';

@Component( {
    selector: 'localized-input',
    templateUrl: './localized-input.component.html',
    styleUrls: [],
} )
export class LocalizedInputComponent {
    @Input() public key: string = "";
    @Input() public value: LocalizedValue;
    @Input() public disabled: boolean = false;
    @Output() public valueChange = new EventEmitter<LocalizedValue>();

    constructor() { }
}