import { NgModule, ModuleWithProviders } from '@angular/core';

import { CommonModule} from '@angular/common';
import { FormsModule} from '@angular/forms';
import { XHRBackend, RequestOptions, Http} from '@angular/http';

import { LocalizeComponent } from './localize/localize.component';
import { LocalizePipe } from './localize/localize.pipe';

import { ErrorMessageComponent } from './message/error-message.component';

import { EventService, IdService, BasicService} from './service/core.service';
import { LocalizationService } from './service/localization.service';
import { EventHttpService } from './service/event-http.service';

import { CookieService } from 'ngx-cookie-service';

import { AuthService } from './auth/auth.service';


@NgModule({
  imports: [
	CommonModule,
	FormsModule,	
    ProgressbarModule.forRoot()       
  ],
  declarations: [
    LocalizeComponent,
    ErrorMessageComponent,
    LocalizePipe,
  ],
  exports: [
    LocalizeComponent,
    LocalizePipe,
  ],
  providers: [
        LocalizationService,
        CookieService, 
        AuthService,
        EventService,
        { 
          provide : EventHttpService,
          useFactory: (xhrBackend: XHRBackend, requestOptions: RequestOptions, service: EventService) => {
            return new EventHttpService(xhrBackend, requestOptions, service)
          },
          deps: [XHRBackend, RequestOptions, EventService]
        }
      ],
  entryComponents: []
})
export class CoreModule {}
