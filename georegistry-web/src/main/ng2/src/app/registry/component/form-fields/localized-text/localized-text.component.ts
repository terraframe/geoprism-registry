import { Component, Input, Output, EventEmitter } from '@angular/core';
import { LocalizedValue } from '@shared/model/core';

@Component( {
    selector: 'localized-text',
    templateUrl: './localized-text.component.html',
    styleUrls: ['./localized-text.css'],
} )
export class LocalizedTextComponent {
    @Input() public key: string = "";
    @Input() public value: LocalizedValue;
    @Input() public disabled: boolean = false;
	@Input() public inlinelayout: boolean = false;
    @Output() public valueChange = new EventEmitter<LocalizedValue>();

    constructor() { }
}