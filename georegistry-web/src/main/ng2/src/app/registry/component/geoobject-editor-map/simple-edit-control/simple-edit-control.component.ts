
import { Component, ElementRef, Input, Output, EventEmitter } from '@angular/core';

@Component({
	selector: 'simple-edit-control',
	templateUrl: './simple-edit-control.component.html',
})
export class SimpleEditControl {
	elRef: ElementRef

	@Output() editEmitter = new EventEmitter<void>();

	@Input() visible: boolean = true;
	@Input() save: boolean = false;

	constructor(elRef: ElementRef) {
		this.elRef = elRef;
	}

	onAdd(map): any {
		//    this.visible = true;
		return this.elRef.nativeElement;
	}

	onRemove(map): void {
		this.elRef.nativeElement.remove();
	}

	onClick(): void {
		this.editEmitter.emit();
	}
}
