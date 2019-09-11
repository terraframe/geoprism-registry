import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';

import { Progress } from '../model/progress';

export interface IProgressListener {
  start(): void;
  progress(progress:Progress):void;  
  complete(): void;
}

@Injectable()
export class ProgressService {
  private listeners: IProgressListener[] = [];
  
  public constructor() {}
  
  public registerListener(listener: IProgressListener): void {
   this.listeners.push(listener);
  }
  
  public deregisterListener(listener: IProgressListener): boolean {
    let indexOfItem = this.listeners.indexOf(listener);

    if (indexOfItem === -1) {
      return false;
    }

    this.listeners.splice(indexOfItem, 1);

    return true;
  }
  
  public start(): void {
    for (const listener of this.listeners) {
      listener.start();
    }
  }
  
  public progress(progress:Progress):void {
    for (const listener of this.listeners) {
      listener.progress(progress);
    }	  
  }  
  
  public complete(): void {
    for (const listener of this.listeners) {
      listener.complete();
    }
  }  
}