import { Component } from '@angular/core';
import { AuthService } from '../core/auth/auth.service';

declare var acp: any;

@Component( {

    selector: 'master-list-header',
    templateUrl: './master-list-header.component.html',
    styleUrls: []
} )
export class MasterListHeaderComponent {
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
