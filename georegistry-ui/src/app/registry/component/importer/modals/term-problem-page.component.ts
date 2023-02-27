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

import { Component, Input, OnInit, EventEmitter, Output } from '@angular/core';
import { ImportConfiguration, TermProblem } from '@registry/model/io';

@Component( {

    selector: 'term-problem-page',
    templateUrl: './term-problem-page.component.html',
    styleUrls: []
} )
export class TermProblemPageComponent implements OnInit {

    @Input() configuration: ImportConfiguration;
    @Output() stateChange = new EventEmitter<string>();
    message: string = null;

    constructor() { }

    ngOnInit(): void {
    }

    hasProblems(): boolean {
        for ( let i = 0; i < this.configuration.termProblems.length; i++ ) {

            if ( !this.configuration.termProblems[i].resolved ) {
                return true;
            }
        }

        return false;
    }

    onNext(): void {
        if ( this.configuration.exclusions == null ) {
            this.configuration.exclusions = [];
        }

        for ( let i = 0; i < this.configuration.termProblems.length; i++ ) {
            const problem = this.configuration.termProblems[i];

            if ( problem.resolved && problem.action.name == 'IGNORE' ) {
                const exclusion = { code: problem.attributeCode, value: problem.label };

                this.configuration.exclusions.push( exclusion );
            }
        }

        this.stateChange.emit( 'NEXT' );
    }

    onCancel(): void {
        this.stateChange.emit( 'CANCEL' );
    }
}
