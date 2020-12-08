import { Component, Input } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';

import { ProfileComponent } from '../profile/profile.component';

import { AuthService, ProfileService } from '@shared/service';

import { RegistryRoleType } from '@shared/model/core';

declare var acp: string;

@Component({

	selector: 'cgr-header',
	templateUrl: './header.component.html',
	styleUrls: []
})
export class CgrHeaderComponent {
	context: string;
	isAdmin: boolean;
	isMaintainer: boolean;
	isContributor: boolean;
	bsModalRef: BsModalRef;

	@Input() loggedIn: boolean = true;

	constructor(
		private modalService: BsModalService,
		private profileService: ProfileService,
		private service: AuthService
	) {
		this.context = acp;
		this.isAdmin = service.isAdmin();
		this.isMaintainer = this.isAdmin || service.isMaintainer();
		this.isContributor = this.isAdmin || this.isMaintainer || service.isContributer();
	}

	shouldShowMenuItem(item: string): boolean {
		if (item === "HIERARCHIES") {
			return true;
		}
		else if (item === "LISTS") {
			//return this.service.hasExactRole(RegistryRoleType.SRA) || this.service.hasExactRole(RegistryRoleType.RA) || this.service.hasExactRole(RegistryRoleType.RM) || this.service.hasExactRole(RegistryRoleType.RC) || this.service.hasExactRole(RegistryRoleType.AC);
			return true;
		}
		else if (this.service.hasExactRole(RegistryRoleType.SRA)) {
			return true;
		}
		else if (item === "IMPORT") {
			return this.service.hasExactRole(RegistryRoleType.RA) || this.service.hasExactRole(RegistryRoleType.RM);
		}
		else if (item === "SCHEDULED-JOBS") {
			return this.service.hasExactRole(RegistryRoleType.RA) || this.service.hasExactRole(RegistryRoleType.RM);
		}
		else if (item === "NAVIGATOR") {
			return this.service.hasExactRole(RegistryRoleType.SRA) || this.service.hasExactRole(RegistryRoleType.RA) || this.service.hasExactRole(RegistryRoleType.RM) || this.service.hasExactRole(RegistryRoleType.RC);
		}
		else if (item === "CHANGE-REQUESTS") {
			return this.service.hasExactRole(RegistryRoleType.RA) || this.service.hasExactRole(RegistryRoleType.RM) || this.service.hasExactRole(RegistryRoleType.RC);
		}
		else if (item === "TASKS") {
			return this.service.hasExactRole(RegistryRoleType.SRA) || this.service.hasExactRole(RegistryRoleType.RA) || this.service.hasExactRole(RegistryRoleType.RM);
		}
		else if (item === "CONFIGS") {
			return this.service.hasExactRole(RegistryRoleType.RA);
		}
		else if (item === "SETTINGS") {
			return true;
		}
		else {
			return false;
		}
	}

	logout(): void {

		window.location.href = acp + '/session/logout';

		//        this.sessionService.logout().then( response => {
		//            this.router.navigate( ['/login'] );
		//        } );
	}

	getUsername() {
		let name: string = this.service.getUsername();

		return name;
	}

	account(): void {
		this.profileService.get().then(profile => {
			this.bsModalRef = this.modalService.show(ProfileComponent, { backdrop: 'static', class: 'gray modal-lg' });
			this.bsModalRef.content.profile = profile;
		});
	}

}
