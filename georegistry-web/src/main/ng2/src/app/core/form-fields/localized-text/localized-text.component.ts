import { Component, Input, Output, EventEmitter } from '@angular/core';
import { LocalizedValue } from '../../../model/registry';

@Component( {
    selector: 'localized-text',
    templateUrl: './localized-text.component.html',
    styleUrls: [],
} )
export class LocalizedTextComponent {
    @Input() public key: string = "";
    @Input() public value: LocalizedValue;
    @Output() public valueChange = new EventEmitter<LocalizedValue>();

    constructor() { }
}