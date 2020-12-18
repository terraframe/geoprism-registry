import { Input, Component, OnInit } from '@angular/core';

import { LocalizationService } from '@shared/service';

@Component({
  
  selector: 'convert-key-label',
  templateUrl: './convert-key-label.component.html',
  styleUrls: []
})
export class ConvertKeyLabel implements OnInit {
  @Input() key: string;
  text: string;
    
  constructor(private service: LocalizationService) { }

  ngOnInit(): void {
	if(this.key === "defaultLocale"){
		this.text = this.service.decode("localization.defaultLocal");
	}
	else{
		this.text = this.key;
	}
  }
}
