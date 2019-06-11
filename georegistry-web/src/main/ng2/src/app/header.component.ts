import { Component } from '@angular/core';
import { AuthService } from './core/auth/auth.service';

declare var acp: any;

@Component( {

    selector: 'cgr-header',
    templateUrl: './header.component.html',
    styleUrls: []
} )
export class CgrHeaderComponent {
    context: string;
    isAdmin: boolean;
    isMaintainer: boolean;
    isContributor: boolean;

    constructor( service: AuthService ) {
        this.context = acp;
        this.isAdmin = service.isAdmin();
        this.isMaintainer = this.isAdmin || service.isMaintainer();
        this.isContributor = this.isAdmin || this.isMaintainer || service.isContributer();
    }

    ngOnInit(): void {

    }

}
