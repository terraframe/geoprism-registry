import { Component } from '@angular/core';
import { AuthService } from '../core/auth/auth.service';

declare var acp: any;

@Component( {

    selector: 'hierarchy-header',
    templateUrl: './hierarchy-header.component.html',
    styleUrls: []
} )
export class HierarchyHeaderComponent {
    private context: string;
    private isAdmin: boolean;
    private isMaintainer: boolean;
    private isContributor: boolean;

    constructor( service: AuthService ) {
        this.context = acp;
        this.isAdmin = service.isAdmin();
        this.isMaintainer = this.isAdmin || service.isMaintainer();
        this.isContributor = this.isAdmin || this.isMaintainer || service.isContributer();
    }

    ngOnInit(): void {

    }

}
