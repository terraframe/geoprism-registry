import { Component, OnInit } from '@angular/core';

import { Progress } from '@shared/model/progress';
import { ProgressService, IProgressListener } from '@shared/service';

@Component({
  
  selector: 'progress-bar',
  templateUrl: './progress-bar.component.html',
  styles: [
    '.progress-overlay { background-color: #CCCCCC; position: absolute; display: block;opacity: 0.8;z-index: 99999 !important;}',
    '.progress-div { width: 100%; margin-left: 0; padding-left: 25%; padding-right: 25%; margin-top: 30% }'
  ]
})
export class ProgressBarComponent implements OnInit, IProgressListener {
  public showIndicator: boolean = true;

  public prog:Progress = {
    current:0,
    total:1,
    description:"Initializing"
  };

  constructor(private service: ProgressService) { }

  ngOnInit(): void {
    this.service.registerListener(this);
  }
  
  ngOnDestroy(): void {
    this.service.deregisterListener(this);
  }
  
  start(): void {
    this.prog = {
      current:0,
      total:1,
      description:"Initializing"
    };
    
    this.showIndicator = true;
  }
  
  progress(progress:Progress):void {
    this.prog = progress;
  }
  
  complete(): void {
    this.showIndicator = false;    
  }    
}
