///
/// Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Geoprism Registry(tm).
///
/// Geoprism Registry(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Geoprism Registry(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
///

import { Component, Input, Output, EventEmitter, ChangeDetectorRef, OnChanges, SimpleChanges } from '@angular/core';

import { FormsModule } from '@angular/forms';

import { AttributeType } from '@registry/model/registry';
import { LocalizedTextComponent } from '../../form-fields/localized-text/localized-text.component';
import { LocalizedInputComponent } from '../../form-fields/localized-input/localized-input.component';
import { GeoObjectAttributeCodeValidator } from '../../../factory/form-validation.factory';
import { LocalizeComponent } from '@shared/component/localize/localize.component';
import { BooleanFieldComponent } from '@shared/component/form-fields/boolean-field/boolean-field.component';
import { TypeaheadMatch, TypeaheadModule } from 'ngx-bootstrap/typeahead';
import { NgClass } from '@angular/common';
import { ConceptSet, ObjectAtTime, ObjectOverTime } from '@registry/model/object-class';
import { Observable, Observer } from 'rxjs';
import { DateFieldComponent } from '@shared/component';
import { ConceptSetService } from '@registry/service/concept-set.service';
import { ConceptObjectService } from '@registry/service/concept-object.service';

@Component({
  selector: 'attribute-input',
  templateUrl: './attribute-input.component.html',
  styleUrls: ['./attribute-input.css'],
  standalone: true,
  imports: [
    NgClass,
    LocalizeComponent,
    FormsModule,
    GeoObjectAttributeCodeValidator,
    LocalizedInputComponent,
    LocalizedTextComponent,
    BooleanFieldComponent,
    DateFieldComponent,
    TypeaheadModule,
  ],
})
export class AttributeInputComponent implements OnChanges {
  @Input() isNew: boolean = false;
  @Input() excludeDescription: boolean = false;
  @Input() type: string | null = null;
  @Input() attribute: AttributeType;
  @Input() manageChangeOverTime: boolean = false;
  @Output() attributeChange = new EventEmitter<AttributeType>();

  message: string | null = null;
  state: string = 'none';

  sets: ConceptSet[] = [];
  setsLoaded: boolean = false;
  currentSet: ConceptSet | null = null;

  typeahead: Observable<ObjectAtTime[]> | null = null;
  text: string = '';
  loading: boolean;

  constructor(
    private cdr: ChangeDetectorRef,
    private setService: ConceptSetService,
    private cObjectService: ConceptObjectService
  ) {}

  ngOnInit(): void {
    this.typeahead = new Observable((observer: Observer<ObjectAtTime[]>) => {
      if (
        this.attribute != null &&
        this.attribute.conceptSet != null &&
        this.attribute.conceptSet.length > 0 &&
        this.attribute.startDate != null &&
        this.attribute.startDate.length > 0
      ) {
        this.cObjectService
          .searchSet(this.attribute.conceptSet, this.attribute.startDate, this.text)
          .then((results) => {
            observer.next(results);
          });
      }
    });
  }

  ngAfterViewInit() {
    this.state = 'show';
    this.cdr.detectChanges();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['attribute'] && this.attribute != null) {
      this.text = this.attribute.rootTerm ?? '';

      this.onConceptSetChange();
    }

    if (changes['type'] && this.type !== 'classification') {
      this.currentSet = null;
    }

    if (changes['type'] && this.type === 'classification' && !this.setsLoaded) {
      this.setsLoaded = true;

      this.setService.getAll().then((sets) => {
        this.sets = sets;

        this.onConceptSetChange();
      });
    }
  }

  ngOnDestroy() {}

  handleOnSubmit(): void {}

  toggleIsUnique(): void {
    this.attribute.unique = !this.attribute.unique;
  }

  animate(): void {
    this.state = 'none';
  }

  onAnimationDone(event: AnimationEvent): void {
    this.state = 'show';
  }

  isValid(): boolean {
    if (this.attribute.code) {
      // if code has a space
      if (this.attribute.code.indexOf(' ') !== -1) {
        return false;
      }

      if (this.attribute.label.localeValues[0].value.length === 0) {
        return false;
      }

      if (this.type === 'float' && (this.attribute.precision == null || this.attribute.precision.toString() === '')) {
        return false;
      }

      if (this.type === 'float' && (this.attribute.scale == null || this.attribute.scale.toString() === '')) {
        return false;
      }

      if (
        this.type === 'classification' &&
        (this.attribute.conceptSet == null || this.attribute.conceptSet.length === 0)
      ) {
        return false;
      }

      if (
        this.type === 'classification' &&
        (this.attribute.startDate == null ||
          this.attribute.startDate.length === 0 ||
          this.attribute.endDate == null ||
          this.attribute.endDate.length === 0)
      ) {
        return false;
      }

      return true;
    }

    return false;
  }

  typeaheadOnSelect(match: TypeaheadMatch): void {
    if (match != null) {
      const item: ObjectOverTime = match.item;

      this.text = item.code;
      this.attribute.rootTerm = item.code;
    } else if (this.attribute.rootTerm != null) {
      this.attribute.rootTerm = null;
    }
  }

  onConceptSetChange(): void {
    if (this.attribute.conceptSet != null && this.attribute.conceptSet.length > 0) {
      this.currentSet = this.sets.find((f) => f.code === this.attribute.conceptSet)!;
    } else {
      this.currentSet = null;
    }
  }

  onTextChange(): void {
    if (this.attribute.rootTerm != null && (this.text == null || this.text.length === 0)) {
      this.attribute.rootTerm = null;
    }
  }
}
