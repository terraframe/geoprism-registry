import { Component, Input } from '@angular/core';

import { TermProblem } from '../shapefile';

@Component( {

    selector: 'term-problem-modal',
    templateUrl: './term-problem-modal.component.html',
    styleUrls: []
} )
export class TermProblemModalComponent {

    @Input() problems: TermProblem[];

    constructor() {
    }
}
