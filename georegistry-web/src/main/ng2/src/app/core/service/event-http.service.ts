import { Injectable } from '@angular/core';
import { Http, RequestOptions, RequestOptionsArgs, Response, ConnectionBackend } from '@angular/http';

import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/finally';

import { EventService } from './core.service';

@Injectable()
export class EventHttpService extends Http {
  private currentRequests: number = 0;

  public constructor(_backend: ConnectionBackend, _defaultOptions: RequestOptions, private service: EventService) {
    super(_backend, _defaultOptions);
  }
  
  public get(url: string, options?: RequestOptionsArgs) : Observable<Response> {
    this.incrementRequestCount();
   
    var response = super.get(url, options).finally(() => {
      this.decrementRequestCount();
    });
    return response;
  }
  
  public post(url: string, body: any, options?: RequestOptionsArgs): Observable<Response> {
    this.incrementRequestCount();
    
    var response = super.post(url, body, options).finally(() => {
      this.decrementRequestCount();
    });
    return response;    
  }

  private decrementRequestCount() {
    if (--this.currentRequests == 0) {
      this.service.complete();
    }
  }

  private incrementRequestCount() {
    if (this.currentRequests++ == 0) {
      this.service.start();
    }
  }
}