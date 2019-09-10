import { Component, OnInit } from '@angular/core';

import { EventService, IEventListener } from '../../service/event.service';

@Component({
  
  selector: 'loading-bar',
  templateUrl: './loading-bar.component.html',
  styleUrls: ['./loading-bar.css']
})
export class LoadingBarComponent implements OnInit, IEventListener {
  showIndicator: boolean = false;

  constructor(private service: EventService) { }

  ngOnInit(): void {
    this.service.registerListener(this);
  }
  
  ngOnDestroy(): void {
    this.service.deregisterListener(this);
  }
  
  start(): void {
    this.showIndicator = true;    
  }
  
  complete(): void {
    this.showIndicator = false;    
  }    
}
