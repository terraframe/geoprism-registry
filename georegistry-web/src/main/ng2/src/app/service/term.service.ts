import { Injectable } from '@angular/core';
import { Headers, Http, Response, URLSearchParams } from '@angular/http';
import 'rxjs/add/operator/toPromise';
import 'rxjs/add/operator/finally';

import { EventService } from '../event/event.service';

declare var acp: string;

@Injectable()
export class TermService {

    constructor( private http: Http, private eventService: EventService ) { }

}
