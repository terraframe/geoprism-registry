
import { Component, ElementRef, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'simple-edit-control',
  templateUrl: './simple-edit-control.component.html',
})
export class SimpleEditControl {
  elRef: ElementRef
  
  @Input() visible = false;
  
  @Output() editEmitter = new EventEmitter<void>();
  
  constructor(elRef: ElementRef) {
    this.elRef = elRef;
  }

  onAdd(map): any {
    this.visible = true;
    return this.elRef.nativeElement;
  }
  
  onRemove(map): void {
    
  }
  
  onClick(): void {
    this.editEmitter.emit();
  }
}
