
import { Injectable } from '@angular/core';
import { HttpHeaders, HttpClient } from '@angular/common/http';

import { finalize } from 'rxjs/operators';

import { EventService } from '../../shared/service/event.service'

declare var acp: any;

@Injectable()
export class ForgotPasswordService {

	constructor(private http: HttpClient, private eventService: EventService) { }

	submit(username: string): Promise<void> {
		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return this.http
			.post<void>(acp + '/forgotpassword/initiate', JSON.stringify({ username: username }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise();
	}

	complete(newPassword: string, token: string): Promise<void> {
		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return this.http
			.post<void>(acp + '/forgotpassword/complete', JSON.stringify({ newPassword: newPassword, token: token }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise();
	}
}
