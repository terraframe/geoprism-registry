import { NgModule, ModuleWithProviders } from '@angular/core';

import { CommonModule} from '@angular/common';
import { FormsModule} from '@angular/forms';
import { XHRBackend, RequestOptions, Http} from '@angular/http';

import { LocalizeComponent } from './localize/localize.component';
import { LocalizePipe } from './localize/localize.pipe';

import { ErrorMessageComponent } from './message/error-message.component';
import { MessageComponent } from './message/message.component';
import { LoadingBarComponent } from './loading-bar/loading-bar.component';
import { EventService, IdService, BasicService} from './service/core.service';
import { LocalizationService } from './service/localization.service';
import { EventHttpService } from './service/event-http.service';

import { PhonePipe } from './phone.pipe';

import { CookieService } from 'ngx-cookie-service';

import { AuthService } from './auth/auth.service';

import { BooleanFieldComponent } from './form-fields/boolean-field/boolean-field.component';


@NgModule({
  imports: [
	  CommonModule,
	  FormsModule
  ],
  declarations: [
    LocalizeComponent,
    LocalizePipe,
    MessageComponent,
    PhonePipe,
    BooleanFieldComponent,
    LoadingBarComponent
  ],
  exports: [
    LocalizeComponent,
    LocalizePipe,
    MessageComponent,
    PhonePipe,
    BooleanFieldComponent,
    LoadingBarComponent
  ],
  entryComponents: [
    LoadingBarComponent
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
      ]
})
export class CoreModule {}
