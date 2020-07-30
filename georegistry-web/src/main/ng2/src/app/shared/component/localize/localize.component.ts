import { Input, Component, OnInit } from '@angular/core';

import { LocalizationService } from '@shared/service/localization.service';

@Component({
  
  selector: 'localize',
  templateUrl: './localize.component.html',
  styleUrls: []
})
export class LocalizeComponent implements OnInit {
  @Input() key: string;
  text: string;
    
  constructor(private service: LocalizationService) { }

  ngOnInit(): void {
    this.text = this.service.decode(this.key);
  }
}
