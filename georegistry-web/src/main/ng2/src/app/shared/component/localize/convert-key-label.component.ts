import { Input, Component, OnInit } from '@angular/core';

import { LocalizationService, AuthService } from '@shared/service';
import { LocaleView } from '@shared/model/core'

@Component({
  
  selector: 'convert-key-label',
  templateUrl: './convert-key-label.component.html',
  styleUrls: []
})
export class ConvertKeyLabel implements OnInit {
  @Input() key: any;
  text: string;
    
  constructor(private service: LocalizationService, private authService: AuthService) { }

  ngOnInit(): void {
    if (this.key != null && this.key.label != null && this.key.label.localizedValue != null)
    {
      this.text = this.key.label.localizedValue;
      return;
    }
    
    let locales: LocaleView[] = this.authService.getLocales();
    
    let len = locales.length;
    for (let i = 0; i < len; ++i)
    {
      let locale: LocaleView = locales[i];
      
      if (locale.toString === this.key)
      {
        this.text = locale.label.localizedValue;
        return;
      }
    }
  
  	if(this.key === "defaultLocale"){
  		this.text = this.service.decode("localization.defaultLocal");
  	}
  	else{
  		this.text = this.key;
  	}
  }
}
